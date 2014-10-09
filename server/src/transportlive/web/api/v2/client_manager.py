# coding=utf-8

from logging import getLogger

from transportlive.web.api.v2.converters import IncomingMessageConverter, OutcomingMessageConverter
import transportlive.web.api.v2.messages as messages

logger = getLogger(__name__)

class ClientManager(object):

    def __init__(self, datastore):
        self._incoming_message_converter = IncomingMessageConverter()
        self._outcoming_message_converter = OutcomingMessageConverter()
        self._clients = {}
        datastore.set_callbacks(self._on_add_vehicle, self._on_remove_vehicle)
        self._datastore = datastore

    def on_data(self, request_handler, text):
        message = self._incoming_message_converter.convert(text)
        if isinstance(message, messages.GreetingMessage):
            logger.debug("New client connected: %s", message.version)
            self._clients[request_handler] = Client(request_handler)
        client = self._clients.get(request_handler)
        if not client:
            return
        if isinstance(message, messages.SelectRouteMessage):
            route = (message.transport_id, message.route_number)
            logger.debug("Route %s selected", route)
            client.selected_routes.add(route)
            for vehicle in self._datastore.get_vehicles(message.transport_id, message.route_number):
                message = self._build_vehicle_message(vehicle)
                message_text = self._outcoming_message_converter.convert(message)
                client.request_handler.write_message(message_text)
        if isinstance(message, messages.UnselectRouteMessage):
            route = (message.transport_id, message.route_number)
            logger.debug("Route %s unselected", route)
            client.selected_routes.remove(route)

    def on_close(self, request_handler):
        if request_handler in self._clients:
            del self._clients[request_handler]

    def _on_add_vehicle(self, vehicle):
        message = self._build_vehicle_message(vehicle)
        message_text = self._outcoming_message_converter.convert(message)
        route = (vehicle.transport.type, vehicle.route.number)
        for client in self._clients.values():
            if route in client.selected_routes:
                client.request_handler.write_message(message_text)

    def _on_remove_vehicle(self, vehicle):
        message = messages.VehicleMessage(vehicle.number, 0, 0, 0, 0, 0)
        message_text = self._outcoming_message_converter.convert(message)
        route = (vehicle.transport.type, vehicle.route.number)
        for client in self._clients.values():
            if route in client.selected_routes:
                client.request_handler.write_message(message_text)

    def _build_vehicle_message(self, vehicle):
        return messages.VehicleMessage(vehicle.number, vehicle.transport.type, vehicle.route.number,
                                       vehicle.last_mark.coords.latitude, vehicle.last_mark.coords.longitude,
                                       vehicle.last_mark.course)

class Client(object):

    def __init__(self, request_handler):
        self.request_handler = request_handler
        self.selected_routes = set()
