# coding=utf-8

import math

from transportlive.forecast.path_store import PathStore

class DirectionDetector(object):

    def __init__(self):
        self._path_store = PathStore()

    def is_straight(self, transport, route, point1, point2):
        path = self._path_store.get(transport, route)
        return self._get_point_position(path, point1) > self._get_point_position(path, point2)

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
        return math.hypot(point1[0] - point2[0], point1[1] - point2[1])
