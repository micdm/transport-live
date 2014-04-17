# coding=utf-8

class LoginPacket(object):

    def __init__(self, login, password):
        self.login = login
        self.password = password

class PingPacket(object):
    pass

class DataPacket(object):

    def __init__(self, imei, datetime, latitude, longitude, speed, course):
        self.imei = imei
        self.datetime = datetime
        self.latitude = latitude
        self.longitude = longitude
        self.speed = speed
        self.course = course
