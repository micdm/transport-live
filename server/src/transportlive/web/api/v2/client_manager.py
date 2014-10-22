from logging import getLogger

from transportlive.web.api.v2.converters import IncomingMessageConverter, OutcomingMessageConverter
import transportlive.web.api.v2.messages as messages

logger = getLogger(__name__)

class ClientManager:

    def __init__(self, datastore):
        self._incoming_message_converter = IncomingMessageConverter()
        self._outcoming_message_converter = OutcomingMessageConverter()
        self._datastore = datastore
        self._selected_routes = {}
        self._selected_stations = {}
        self._forecasts = {}

    def init(self):
        self._datastore.set_callbacks(self._on_update_vehicle, self._on_remove_vehicle)

    def on_data(self, request_handler, text):
        message = self._incoming_message_converter.convert(text)
        if isinstance(message, messages.GreetingMessage):
            self._handle_greeting_message(request_handler, message)
        if isinstance(message, messages.SelectRouteMessage):
            self._handle_select_route_message(request_handler, message)
        if isinstance(message, messages.UnselectRouteMessage):
            self._handle_unselect_route_message(request_handler, message)
        if isinstance(message, messages.SelectStationMessage):
            self._handle_select_station_message(request_handler, message)
        if isinstance(message, messages.UnselectStationMessage):
            self._handle_unselect_station_message(request_handler, message)
        if isinstance(message, messages.LoadNearestStationsMessage):
            self._handle_load_nearest_stations_message(request_handler, message)

    def _handle_greeting_message(self, request_handler, message):
        logger.info("New client connected: %s (%s)", request_handler, message.version)

    def _handle_select_route_message(self, request_handler, message):
        route = (message.transport_id, message.route_number)
        request_handlers = self._selected_routes.get(route)
        if not request_handlers:
            request_handlers = []
            self._selected_routes[route] = request_handlers
        if request_handler not in request_handlers:
            logger.debug("Route %s selected", route)
            self._selected_routes[route].append(request_handler)
            self._send_initial_vehicles(request_handler, message.transport_id, message.route_number)

    def _send_initial_vehicles(self, request_handler, transport_id, route_number):
        for vehicle in self._datastore.get_vehicles(transport_id, route_number):
            message = self._build_vehicle_message(vehicle)
            message_text = self._outcoming_message_converter.convert(message)
            request_handler.write_message(message_text)

    def _handle_unselect_route_message(self, request_handler, message):
        route = (message.transport_id, message.route_number)
        logger.debug("Route %s unselected", route)
        self._remove_request_handler_from_route_subscription(request_handler, route)

    def _handle_select_station_message(self, request_handler, message):
        station = (message.transport_id, message.station_id)
        request_handlers = self._selected_stations.get(station)
        if not request_handlers:
            request_handlers = []
            self._selected_stations[station] = request_handlers
        if request_handler not in request_handlers:
            logger.debug("Station %s selected", station)
            self._selected_stations[station].append(request_handler)
            self._send_initial_forecast(request_handler, message.transport_id, message.station_id)

    def _send_initial_forecast(self, request_handler, transport_id, station_id):
        forecast = self._datastore.get_forecast(transport_id, station_id)
        message = self._build_forecast_message(forecast)
        message_text = self._outcoming_message_converter.convert(message)
        request_handler.write_message(message_text)

    def _handle_unselect_station_message(self, request_handler, message):
        station = (message.transport_id, message.station_id)
        logger.debug("Station %s unselected", station)
        self._remove_request_handler_from_station_subscription(request_handler, station)

    def on_close(self, request_handler):
        logger.debug("Closing connection %s...", request_handler)
        for route in list(self._selected_routes.keys()):
            self._remove_request_handler_from_route_subscription(request_handler, route)
        for station in list(self._selected_stations.keys()):
            self._remove_request_handler_from_station_subscription(request_handler, station)

    def _remove_request_handler_from_route_subscription(self, request_handler, route):
        request_handlers = self._selected_routes[route]
        if request_handler in request_handlers:
            request_handlers.remove(request_handler)
            if not request_handlers:
                del self._selected_routes[route]

    def _remove_request_handler_from_station_subscription(self, request_handler, station):
        request_handlers = self._selected_stations[station]
        if request_handler in request_handlers:
            request_handlers.remove(request_handler)
            if not request_handlers:
                del self._selected_stations[station]

    def _on_update_vehicle(self, vehicle):
        self._send_update_vehicle_messages(vehicle)
        self._send_update_forecast_messages(vehicle)

    def _send_update_vehicle_messages(self, vehicle):
        message = self._build_vehicle_message(vehicle)
        message_text = self._outcoming_message_converter.convert(message)
        route = (vehicle.transport.type, vehicle.route.number)
        request_handlers = self._selected_routes.get(route, [])
        for request_handler in request_handlers:
            request_handler.write_message(message_text)

    def _send_update_forecast_messages(self, vehicle):
        stations = self._get_probably_affected_stations(vehicle.transport.type)
        for station in stations:
            transport_id, station_id = station
            forecast = self._datastore.get_forecast(transport_id, station_id)
            if not self._is_forecast_changed(station, forecast):
                continue
            message = self._build_forecast_message(forecast)
            message_text = self._outcoming_message_converter.convert(message)
            for request_handler in self._selected_stations[station]:
                request_handler.write_message(message_text)
            self._forecasts[station] = forecast

    def _get_probably_affected_stations(self, transport_id):
        stations = []
        for station in self._selected_stations:
            if station[0] == transport_id:
                stations.append(station)
        return stations

    def _is_forecast_changed(self, station, forecast):
        original_forecast = self._forecasts.get(station)
        if not original_forecast:
            return True
        for vehicle in forecast.vehicles:
            original_vehicle = self._get_forecast_vehicle(original_forecast, vehicle["vehicle"].id)
            if not original_vehicle:
                return True
            if vehicle["arrival_time"] != original_vehicle["arrival_time"]:
                return True
        for original_vehicle in original_forecast.vehicles:
            vehicle = self._get_forecast_vehicle(forecast, original_vehicle["vehicle"].id)
            if not vehicle:
                return True
        return False

    def _get_forecast_vehicle(self, forecast, vehicle_id):
        for vehicle in forecast.vehicles:
            if vehicle["vehicle"].id == vehicle_id:
                return vehicle
        return None

    def _on_remove_vehicle(self, vehicle):
        self._send_remove_vehicle_messages(vehicle)
        self._send_update_forecast_messages(vehicle)

    def _send_remove_vehicle_messages(self, vehicle):
        message = messages.VehicleMessage(vehicle.number, 0, 0, 0, 0, 0)
        message_text = self._outcoming_message_converter.convert(message)
        route = (vehicle.transport.type, vehicle.route.number)
        request_handlers = self._selected_routes.get(route, [])
        for request_handler in request_handlers:
            request_handler.write_message(message_text)

    def _build_vehicle_message(self, vehicle):
        return messages.VehicleMessage(vehicle.number, vehicle.transport.type, vehicle.route.number,
                                       vehicle.last_mark.coords.latitude, vehicle.last_mark.coords.longitude,
                                       vehicle.last_mark.course)

    def _build_forecast_message(self, forecast):
        vehicles = [(vehicle["vehicle"].number, vehicle["vehicle"].route.number, vehicle["arrival_time"],
                     vehicle["vehicle"].is_low_floor) for vehicle in forecast.vehicles]
        return messages.ForecastMessage(forecast.transport.type, forecast.station.id, vehicles)

    def _handle_load_nearest_stations_message(self, request_handler, message):
        logger.debug("Finding nearest stations for (%s, %s)", message.latitude, message.longitude)
        stations = self._datastore.get_nearest_stations(message.latitude, message.longitude)
        message = messages.NearestStationsMessage((station["transport"].type, station["station"].id) for station in stations)
        message_text = self._outcoming_message_converter.convert(message)
        request_handler.write_message(message_text)
