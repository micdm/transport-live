# coding=utf-8

class Station(object):

    def __init__(self, station_id, latitude, longitude, name):
        self.id = station_id
        self.latitude = latitude
        self.longitude = longitude
        self.name = name
