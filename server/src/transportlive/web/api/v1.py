# coding=utf-8

from tornado.web import RequestHandler

class VehicleHandler(RequestHandler):

    def initialize(self, datastore):
        self._datastore = datastore

    def get(self):
        request = self.get_arguments("route")
        vehicles = self._get_vehicles(request)
        self.finish(self._result_to_dict(vehicles))

    def _get_vehicles(self, request):
        vehicles = {}
        for transport_type, route_number in map(lambda item: map(int, item.split("-")), request):
            if transport_type not in vehicles:
                vehicles[transport_type] = {}
            vehicles[transport_type][route_number] = self._datastore.get_vehicles(transport_type, route_number)
        return vehicles

    def _result_to_dict(self, result):
        return {
            "transports": map(self._transport_to_dict, result.keys(), result.values())
        }

    def _transport_to_dict(self, transport_type, routes):
        return {
            "type": transport_type,
            "routes": map(self._route_to_dict, routes.keys(), routes.values())
        }

    def _route_to_dict(self, route_number, vehicles):
        return {
            "number": route_number,
            "vehicles": map(self._vehicle_to_dict, vehicles)
        }

    def _vehicle_to_dict(self, vehicle):
        last_mark = vehicle.last_mark
        coords = last_mark.coords
        return {
            "lat": str(coords.latitude),
            "lon": str(coords.longitude),
            "course": last_mark.course
        }

class ForecastHandler(RequestHandler):

    def initialize(self, datastore):
        self._datastore = datastore

    def get(self):
        request = self.get_arguments("station")
        forecasts = self._get_forecasts(request)
        self.finish(self._result_to_dict(forecasts))

    def _get_forecasts(self, request):
        forecasts = []
        for transport_type, station_id in map(lambda item: map(int, item.split("-")), request):
            forecast = self._datastore.get_forecast(transport_type, station_id)
            forecasts.append(forecast)
        return forecasts

    def _result_to_dict(self, result):
        return {
            "stations": map(self._forecast_to_dict, result)
        }

    def _forecast_to_dict(self, forecast):
        return {
            "transport": forecast.transport.type,
            "id": forecast.station.id,
            "vehicles": map(self._vehicle_to_dict, forecast.vehicles)
        }

    def _vehicle_to_dict(self, vehicle):
        return {
            "transport": vehicle.transport.type,
            "route": vehicle.route.number,
            "time": vehicle.time
        }
