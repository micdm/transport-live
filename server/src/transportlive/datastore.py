# coding=utf-8

from datetime import datetime
from logging import getLogger

from tornado.options import options

from transportlive.forecast.forecast_calculator import ForecastCalculator
from transportlive.misc.low_floor_vehicles_builder import LowFloorVehiclesBuilder
from transportlive.misc.service_builder import ServiceBuilder
from transportlive.models import Vehicle

logger = getLogger(__name__)

class DataStore(object):

    MAX_MARK_COUNT = 30

    def __init__(self):
        self._service = ServiceBuilder().build()
        self._low_floor_vehicles = LowFloorVehiclesBuilder().build()
        self._vehicles = {}
        self._forecast_calculator = ForecastCalculator(self._service)

    def add_vehicle(self, info):
        vehicle = self._vehicles.get(info.vehicle_id)
        if not vehicle:
            vehicle = Vehicle(info.vehicle_id, info.number, self._is_low_floor(info.transport_type, info.number))
            self._vehicles[info.vehicle_id] = vehicle
        vehicle.transport = self._service.get_transport_by_type(info.transport_type)
        vehicle.route = vehicle.transport.get_route_by_number(info.route_number)
        vehicle.marks.append(info.mark)
        self._forecast_calculator.update_vehicle(vehicle)

    def _is_low_floor(self, transport_type, number):
        return transport_type in self._low_floor_vehicles and number in self._low_floor_vehicles[transport_type]

    def get_vehicles(self, transport_type, route_number):
        transport = self._service.get_transport_by_type(transport_type)
        route = transport.get_route_by_number(route_number)
        return filter(lambda vehicle: vehicle.transport == transport and vehicle.route == route, self._vehicles.values())

    def get_forecast(self, transport_type, station_id):
        return self._forecast_calculator.get_forecast(transport_type, station_id)

    def cleanup(self):
        logger.info("Cleaning up datastore...")
        self._remove_outdated_vehicles()
        self._remove_unnecessary_vehicle_marks()

    def _remove_outdated_vehicles(self):
        time = datetime.utcnow() - options.VEHICLE_OUTDATE_INTERVAL
        logger.debug("Removing vehicles last updated before %s...", time)
        count = 0
        for vehicle_id, vehicle in dict(self._vehicles).items():
            if vehicle.last_mark.datetime < time:
                self._forecast_calculator.remove_vehicle(vehicle)
                del self._vehicles[vehicle_id]
                count += 1
        logger.debug("Removed %s vehicles", count)

    def _remove_unnecessary_vehicle_marks(self):
        logger.debug("Removing unnecessary vehicle marks...")
        count = 0
        for vehicle in self._vehicles.values():
            marks = vehicle.marks[-self.MAX_MARK_COUNT:]
            count += len(vehicle.marks) - len(marks)
            vehicle.marks = marks
        logger.debug("Removed %s marks", count)
