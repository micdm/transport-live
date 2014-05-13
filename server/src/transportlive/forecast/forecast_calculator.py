# coding=utf-8

import math

from transportlive.models import Forecast, ForecastVehicle, Coords

class ForecastCalculator(object):

    def __init__(self, service, vehicles):
        self._station_index = StationIndex.create(service)
        self._direction_detector = DirectionDetector()
        self._distance_calculator = DistanceCalculator()
        self._service = service
        self._vehicles = vehicles

    def get_forecast(self, station_id):
        station = self._service.get_station_by_id(station_id)
        forecast = Forecast(station)
        for transport, route, vehicle, distance in self._get_vehicles(station):
            average_speed = self._get_vehicle_average_speed(vehicle)
            if not average_speed:
                continue
            forecast.vehicles.append(ForecastVehicle(transport, route, distance / average_speed))
        return forecast

    def _get_vehicle_average_speed(self, vehicle):
        marks = filter(lambda mark: mark.speed != 0, vehicle.marks)
        if not marks:
            return None
        return sum(mark.speed for mark in marks) / len(marks)

    def _get_vehicles(self, station):
        for transport, route, direction in self._station_index.get(station):
            for vehicle in self._vehicles.get_by_transport_and_route(transport, route):
                if len(vehicle.marks) < 2:
                    continue
                if self._direction_detector.get_direction(route, vehicle.marks[0].coords, vehicle.marks[-1].coords) != direction:
                    continue
                station_distance = self._distance_calculator.get_distance(route, direction, station.coords)
                vehicle_distance = self._distance_calculator.get_distance(route, direction, vehicle.last_mark.coords)
                distance = station_distance - vehicle_distance
                if distance > 0:
                    yield transport, route, vehicle, distance

class StationIndex(object):

    @classmethod
    def create(cls, service):
        index = {}
        for transport in service.transports:
            for route in transport.routes:
                for direction in route.directions:
                    for station in direction.stations:
                        cls._add_to_index(station, transport, route, direction, index)
        return cls(index)

    @classmethod
    def _add_to_index(cls, station, transport, route, direction, index):
        if station not in index:
            index[station] = []
        index[station].append((transport, route, direction))

    def __init__(self, index):
        self._index = index

    def get(self, station_id):
        return self._index[station_id]

class NearestPointFinder(object):

    def get_nearest(self, path, point):
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

class DirectionDetector(object):

    def __init__(self):
        self._nearest_point_finder = NearestPointFinder()

    def get_direction(self, route, point1, point2):
        begin = self._nearest_point_finder.get_nearest(route.points, point1)
        end = self._nearest_point_finder.get_nearest(route.points, point2)
        is_straight = end > begin
        for direction in route.directions:
            if direction.is_straight == is_straight:
                return direction
        raise Exception("unknown direction")

class DistanceCalculator(object):

    EARTH_RADIUS = 6371

    def __init__(self):
        self._nearest_point_finder = NearestPointFinder()
        self._point_index = {}

    def get_distance(self, route, direction, point):
        path = route.points if direction.is_straight else route.points[::-1]
        nearest = self._nearest_point_finder.get_nearest(path, point)
        projection = self._get_projection(path[nearest], path[nearest + 1], point)
        return self._get_path_length(path, path[nearest]) + self._get_distance_between_points(path[nearest], projection)

    def _get_path_length(self, path, point):
        if point not in self._point_index:
            index = path.index(point)
            if index == 0:
                distance = 0
            else:
                previous = path[index - 1]
                distance = self._get_distance_between_points(point, previous) + self._get_path_length(path, previous)
            self._point_index[point] = distance
        return self._point_index[point]

    def _get_projection(self, begin, end, point):
        if begin.latitude == end.latitude:
            return Coords(begin.latitude, point.longitude)
        if begin.longitude == end.longitude:
            return Coords(point.latitude, begin.longitude)
        k = (end.longitude - begin.longitude) / (end.latitude - begin.latitude)
        latitude = (k * begin.latitude + point.latitude / k - begin.longitude + point.longitude) / (k + 1 / k)
        longitude = k * (latitude - begin.latitude) + begin.longitude
        return Coords(latitude, longitude)

    def _get_distance_between_points(self, point1, point2):
        lat1, lon1 = math.radians(point1.latitude), math.radians(point1.longitude)
        lat2, lon2 = math.radians(point2.latitude), math.radians(point2.longitude)
        radians = math.acos(math.sin(lat1) * math.sin(lat2) + math.cos(lat1) * math.cos(lat2) * math.cos(lon1 - lon2))
        return radians * self.EARTH_RADIUS
