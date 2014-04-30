# coding=utf-8

import math

from transportlive.forecast.service_builder import ServiceBuilder

class DirectionDetector(object):

    def __init__(self):
        self._service = None

    def get_direction(self, transport_type, route_number, point1, point2):
        route = self._get_route(transport_type, route_number)
        is_straight = self._get_point_position(route.points, point1) > self._get_point_position(route.points, point2)
        return route.directions[0] if is_straight else route.directions[1]

    def _get_route(self, transport_type, route_number):
        service = self._get_service()
        transport = service.get_transport_by_type(transport_type)
        return transport.get_route_by_number(route_number)

    def _get_service(self):
        if not self._service:
            self._service = ServiceBuilder().build()
        return self._service

    def _get_point_position(self, path, point):
        i = 0
        result = i
        min_distance = float("inf")
        while i < len(path) - 1:
            begin, end = path[i], path[i + 1]
            distance = self._get_distance(point, begin) + self._get_distance(point, end)
            if distance < min_distance:
                result = i
                min_distance = distance
            i += 1
        return result

    def _get_distance(self, point1, point2):
        return math.hypot(point1.latitude - point2.latitude, point1.longitude - point2.longitude)
