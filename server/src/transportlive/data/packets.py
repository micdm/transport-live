class LoginPacket:

    def __init__(self, login, password):
        self.login = login
        self.password = password

class LoginAnswerPacket:

    STATUS_OK = 0

    def __init__(self, status):
        self.status = status

class PingPacket:
    pass

class PingAnswerPacket:
    pass

class DataPacket:

    def __init__(self, imei, datetime_created, latitude, longitude, speed, course, params):
        self.imei = imei
        self.datetime_created = datetime_created
        self.latitude = latitude
        self.longitude = longitude
        self.speed = speed
        self.course = course
        self.params = params

class DataAnswerPacket:

    STATUS_OK = 0

    def __init__(self, status):
        self.status = status
