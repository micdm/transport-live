# coding=utf-8

class Service(object):

    def __init__(self):
        self.stations = []
        self.transports = []

    def get_station_by_id(self, station_id):
        for station in self.stations:
            if station.id == station_id:
                return station
        raise Exception('unknown station "%s"'%station_id)

    def get_transport_by_type(self, transport_type):
        for transport in self.transports:
            if transport.type == transport_type:
                return transport
        raise Exception('unknown transport "%s"'%transport_type)
