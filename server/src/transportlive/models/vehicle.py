# coding=utf-8

class Vehicle(object):

    def __init__(self, vehicle_id, transport, route, latitude, longitude, speed, course):
        self.id = vehicle_id
        self.transport = transport
        self.route = route
        self.latitude = latitude
        self.longitude = longitude
        self.speed = speed
        self.course = course
