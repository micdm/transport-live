# coding=utf-8

from logging import getLogger

from tornado.web import RequestHandler

logger = getLogger(__name__)

class VehicleHandler(RequestHandler):

    def initialize(self, datastore):
        self._datastore = datastore

    def get(self):
        request = self.get_arguments("route")
        vehicles = self._get_vehicles(request)
        self.finish(vehicles)

    def _get_vehicles(self, request):
        result = {}
        for transport, route in map(lambda item: map(int, item.split("-")), request):
            if transport not in result:
                result[transport] = {}
            if route not in result[transport]:
                result[transport][route] = []
            for vehicle in self._datastore.get_vehicles(transport, route):
                result[transport][route].append({
                    "lat": str(vehicle.latitude),
                    "lon": str(vehicle.longitude),
                    "dir": str(vehicle.course)
                })
        return result
