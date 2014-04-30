# coding=utf-8

class Transport(object):

    TYPE_TROLLEYBUS = 0
    TYPE_TRAM = 1

    def __init__(self, transport_type):
        self.type = transport_type
        self.routes = []

    def get_route_by_number(self, number):
        for route in self.routes:
            if route.number == number:
                return route
        raise Exception('unknown route "%s"'%number)
