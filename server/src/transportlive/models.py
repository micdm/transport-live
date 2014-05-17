# coding=utf-8

class Service(object):

    def __init__(self):
        self.transports = []

    def get_transport_by_type(self, transport_type):
        for transport in self.transports:
            if transport.type == transport_type:
                return transport
        raise Exception('unknown transport "%s"'%transport_type)

class Transport(object):

    TYPE_TROLLEYBUS = 0
    TYPE_TRAM = 1

    def __init__(self, transport_type):
        self.type = transport_type
        self.stations = []
        self.routes = []

    def get_station_by_id(self, station_id):
        for station in self.stations:
            if station.id == station_id:
                return station
        raise Exception('unknown station "%s"'%station_id)

    def get_route_by_number(self, number):
        for route in self.routes:
            if route.number == number:
                return route
        raise Exception('unknown route "%s"'%number)

class Station(object):

    def __init__(self, station_id, coords, name):
        self.id = station_id
        self.coords = coords
        self.name = name

class Route(object):

    def __init__(self, number):
        self.number = number
        self.directions = []

class Direction(object):

    def __init__(self, direction_id):
        self.id = direction_id
        self.stations = []
        self.points = []

class Vehicle(object):

    def __init__(self, vehicle_id, number):
        self.id = vehicle_id
        self.number = number
        self.transport = None
        self.route = None
        self.marks = []

    @property
    def last_mark(self):
        return self.marks[-1] if len(self.marks) else None

class Mark(object):

    def __init__(self, datetime, coords, speed, course):
        self.datetime = datetime
        self.coords = coords
        self.speed = speed
        self.course = course

class Coords(object):

    def __init__(self, latitude, longitude):
        self.latitude = latitude
        self.longitude = longitude

class Forecast(object):

    def __init__(self, transport, station):
        self.transport = transport
        self.station = station
        self.vehicles = []

class ForecastVehicle(object):

    def __init__(self, route, time):
        self.route = route
        self.time = time
