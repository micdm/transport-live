# coding=utf-8

from logging import getLogger
import math

from transportlive.models import Forecast, ForecastVehicle, Coords

logger = getLogger(__name__)

_EARTH_RADIUS = 6371000

def _get_nearest_point_index(points, point):
    result = i = 0
    min_distance = float('inf')
    while i < len(points) - 1:
        begin, end = points[i], points[i + 1]
        projection = _get_projection(begin, end, point)
        if _is_point_inside_segment(begin, end, projection):
            distance = _get_distance_in_meters(point, projection)
            if distance < min_distance:
                min_distance = distance
                result = i
        i += 1
    return result

def _get_projection(begin, end, point):
    if begin.latitude == end.latitude:
        return Coords(begin.latitude, point.longitude)
    if begin.longitude == end.longitude:
        return Coords(point.latitude, begin.longitude)
    k = (end.longitude - begin.longitude) / (end.latitude - begin.latitude)
    latitude = (k * begin.latitude + point.latitude / k - begin.longitude + point.longitude) / (k + 1 / k)
    longitude = k * (latitude - begin.latitude) + begin.longitude
    return Coords(latitude, longitude)

def _is_point_inside_segment(begin, end, point):
    if begin.latitude <= point.latitude <= end.latitude and begin.longitude <= point.longitude <= end.longitude:
        return True
    if end.latitude <= point.latitude <= begin.latitude and begin.longitude <= point.longitude <= end.longitude:
        return True
    if begin.latitude <= point.latitude <= end.latitude and end.longitude <= point.longitude <= begin.longitude:
        return True
    if end.latitude <= point.latitude <= begin.latitude and end.longitude <= point.longitude <= begin.longitude:
        return True
    return False

def _get_distance_in_meters(point1, point2):
    lat1, lon1 = math.radians(point1.latitude), math.radians(point1.longitude)
    lat2, lon2 = math.radians(point2.latitude), math.radians(point2.longitude)
    radians = math.acos(math.sin(lat1) * math.sin(lat2) + math.cos(lat1) * math.cos(lat2) * math.cos(lon1 - lon2))
    return round(radians * _EARTH_RADIUS, 2)

class ForecastCalculator(object):

    def __init__(self, service):
        self._direction_calculator = _DirectionCalculator()
        self._distance_calculator = _DistanceCalculator(service)
        self._station_index = _StationIndex.create(service)
        self._service = service

    def get_forecast(self, transport_type, station_id):
        transport = self._service.get_transport_by_type(transport_type)
        station = transport.get_station_by_id(station_id)
        logger.info('Building forecast for station "%s" (%s-%s)...', station.name, transport_type, station.id)
        forecast = Forecast(transport, station)
        for route, vehicle, distance in self._get_vehicles(station):
            speed = self._get_vehicle_speed(vehicle)
            if not speed:
                continue
            # TODO: возвращать время в секундах
            time = int(distance / speed) / 60
            forecast.vehicles.append(ForecastVehicle(transport, route, time))
        return forecast

    def _get_vehicles(self, station):
        for transport, route, direction in self._station_index.get(station):
            for vehicle in self._direction_calculator.get_vehicles(direction):
                station_distance = self._distance_calculator.get_distance(direction, station.coords)
                vehicle_distance = self._distance_calculator.get_distance(direction, vehicle.last_mark.coords)
                distance = station_distance - vehicle_distance
                if distance > 0:
                    yield route, vehicle, distance

    def _get_vehicle_speed(self, vehicle):
        marks = self._get_non_zero_speed_vehicle_marks(vehicle)
        if not marks:
            return None
        return sum(mark.speed for mark in marks) / len(marks)

    def _get_non_zero_speed_vehicle_marks(self, vehicle):
        return filter(lambda mark: mark.speed != 0, vehicle.marks)

    def update_vehicle(self, vehicle):
        self._direction_calculator.update_vehicle(vehicle)

    def remove_vehicle(self, vehicle):
        self._direction_calculator.remove_vehicle(vehicle)

    def cleanup(self):
        self._direction_calculator.cleanup()
        self._distance_calculator.cleanup()

class _DirectionCalculator(object):

    def __init__(self):
        self._nearest_point_index_cache = {}
        self._vehicles = {}

    def get_vehicles(self, direction):
        return self._vehicles.get(direction)

    def update_vehicle(self, vehicle):
        self.remove_vehicle(vehicle)
        direction = self._get_vehicle_direction(vehicle)
        if direction:
            if direction not in self._vehicles:
                self._vehicles[direction] = []
            self._vehicles[direction].append(vehicle)

    def _get_vehicle_direction(self, vehicle):
        for mark in vehicle.marks[-2::-1]:
            direction = self._get_direction(vehicle.route, mark.coords, vehicle.last_mark.coords)
            if direction:
                return direction
        return None

    def _get_direction(self, route, point1, point2):
        direction = route.directions[0]
        begin = self._get_nearest_point_index(direction.points, point1)
        end = self._get_nearest_point_index(direction.points, point2)
        if begin == end:
            return None
        return direction if end > begin else route.directions[1]

    def _get_nearest_point_index(self, points, point):
        if point not in self._nearest_point_index_cache:
            self._nearest_point_index_cache[point] = _get_nearest_point_index(points, point)
        return self._nearest_point_index_cache[point]

    def remove_vehicle(self, vehicle):
        for direction_vehicles in self._vehicles.values():
            if vehicle in direction_vehicles:
                direction_vehicles.remove(vehicle)

    def cleanup(self):
        self._nearest_point_index_cache.clear()

class _DistanceCalculator(object):

    def __init__(self, service):
        self._distance_index = _DistanceIndex.create(service)
        self._distance_cache = {}

    def get_distance(self, direction, point):
        key = "%s-%s"%(direction, point)
        if key not in self._distance_cache:
            self._distance_cache[key] = self._get_distance(direction, point)
        return self._distance_cache[key]

    def _get_distance(self, direction, point):
        nearest = _get_nearest_point_index(direction.points, point)
        projection = _get_projection(direction.points[nearest], direction.points[nearest + 1], point)
        return self._distance_index.get(direction.points[nearest]) + _get_distance_in_meters(direction.points[nearest], projection)

    def cleanup(self):
        self._distance_cache.clear()

class _DistanceIndex(object):

    @classmethod
    def create(cls, service):
        logger.info("Building distance index...")
        index = {}
        for transport in service.transports:
            for route in transport.routes:
                for direction in route.directions:
                    cls._add_to_index(direction.points[0], 0, index)
                    distance = 0
                    for i, point in enumerate(direction.points[1:]):
                        distance += _get_distance_in_meters(point, direction.points[i - 1])
                        cls._add_to_index(point, distance, index)
        logger.info("Distance index ready")
        return cls(index)

    @classmethod
    def _add_to_index(cls, point, distance, index):
        index[point] = distance

    def __init__(self, index):
        self._index = index

    def get(self, point):
        return self._index[point]

class _StationIndex(object):

    @classmethod
    def create(cls, service):
        logger.info("Building station index...")
        index = {}
        for transport in service.transports:
            for route in transport.routes:
                for direction in route.directions:
                    for station in direction.stations:
                        cls._add_to_index(station, transport, route, direction, index)
        logger.info("Station index ready")
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
