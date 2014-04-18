# coding=utf-8

class LoginPacket(object):

    def __init__(self, login, password):
        self.login = login
        self.password = password

class LoginAnswerPacket(object):

    STATUS_OK = 0

    def __init__(self, status):
        self.status = status

class PingPacket(object):
    pass

class PingAnswerPacket(object):
    pass

class DataPacket(object):

    def __init__(self, imei, datetime, latitude, longitude, speed, course):
        self.imei = imei
        self.datetime = datetime
        self.latitude = latitude
        self.longitude = longitude
        self.speed = speed
        self.course = course

class DataAnswerPacket(object):

    STATUS_OK = 0

    def __init__(self, status):
        self.status = status
