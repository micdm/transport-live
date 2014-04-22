# coding=utf-8

# TODO: периодически подчищать от устаревших данных
class DataStore(object):

    def __init__(self):
        self._vehicles = {}

    def get_vehicles(self, transport, route):
        return filter(lambda vehicle: vehicle.transport == transport and vehicle.route == route, self._vehicles.values())

    def add_vehicle(self, vehicle):
        self._vehicles[vehicle.id] = vehicle
