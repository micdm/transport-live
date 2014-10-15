class Message:
    pass

class GreetingMessage(Message):

    def __init__(self, version):
        self.version = version

class SelectRouteMessage(Message):

    def __init__(self, transport_id, route_number):
        self.transport_id = transport_id
        self.route_number = route_number

class UnselectRouteMessage(Message):

    def __init__(self, transport_id, route_number):
        self.transport_id = transport_id
        self.route_number = route_number

class SelectStationMessage(Message):

    def __init__(self, transport_id, station_id):
        self.transport_id = transport_id
        self.station_id = station_id

class UnselectStationMessage(Message):

    def __init__(self, transport_id, station_id):
        self.transport_id = transport_id
        self.station_id = station_id

class LoadNearestStationsMessage(Message):

    def __init__(self, latitude, longitude):
        self.latitude = latitude
        self.longitude = longitude

class VehicleMessage(Message):

    def __init__(self, number, transport_id, route_number, latitude, longitude, course):
        self.number = number
        self.transport_id = transport_id
        self.route_number = route_number
        self.latitude = latitude
        self.longitude = longitude
        self.course = course

class ForecastMessage(Message):

    def __init__(self, transport_id, station_id, vehicles):
        self.transport_id = transport_id
        self.station_id = station_id
        self.vehicles = vehicles

class NearestStationsMessage(Message):

    def __init__(self, stations):
        self.stations = stations
