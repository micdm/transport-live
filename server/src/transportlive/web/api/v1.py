from tornado.web import RequestHandler

class VehicleHandler(RequestHandler):

    def initialize(self, datastore):
        self._datastore = datastore

    def get(self):
        request = self.get_arguments("route")
        vehicles = self._get_vehicles(request)
        self.finish(self._result_to_dict(vehicles))

    def _get_vehicles(self, request):
        result = []
        for transport_type, route_number in map(lambda item: map(int, item.split("-")), request):
            vehicles = self._datastore.get_vehicles(transport_type, route_number)
            if vehicles:
                result.append(vehicles)
        return result

    def _result_to_dict(self, result):
        return {
            "result": map(self._vehicles_to_dict, result)
        }

    def _vehicles_to_dict(self, vehicles):
        return {
            "transport": vehicles[0].transport.type,
            "route": vehicles[0].route.number,
            "vehicles": map(self._vehicle_to_dict, vehicles)
        }

    def _vehicle_to_dict(self, vehicle):
        last_mark = vehicle.last_mark
        coords = last_mark.coords
        result = {
            "number": vehicle.number,
            "lat": str(coords.latitude),
            "lon": str(coords.longitude),
            "course": last_mark.course
        }
        if vehicle.is_low_floor:
            result["is_low_floor"] = True
        return result

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
            "result": map(self._forecast_to_dict, result)
        }

    def _forecast_to_dict(self, forecast):
        return {
            "transport": forecast.transport.type,
            "station": forecast.station.id,
            "vehicles": map(lambda item: self._vehicles_to_dict(item["vehicle"], item["arrival_time"]), forecast.vehicles)
        }

    def _vehicles_to_dict(self, vehicle, time):
        result = {
            "route": vehicle.route.number,
            "time": time
        }
        if vehicle.is_low_floor:
            result["is_low_floor"] = True
        return result
