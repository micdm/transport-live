# coding=utf-8

from datetime import datetime, timedelta
from logging import getLogger

from transportlive.forecast.forecast_calculator import ForecastCalculator
from transportlive.forecast.service_builder import ServiceBuilder
from transportlive.models import Vehicle

logger = getLogger(__name__)

class DataStore(object):

    MAX_MARK_COUNT = 10
    VEHICLE_OUTDATE_INTERVAL = timedelta(minutes=1)

    def __init__(self):
        self._service = ServiceBuilder().build()
        self._vehicles = VehicleCollection()
        self._forecast_calculator = ForecastCalculator(self._service, self._vehicles)

    def add_vehicle(self, vehicle_id, transport_type, route_number, mark):
        vehicle = self._vehicles.get(vehicle_id)
        if not vehicle:
            vehicle = Vehicle(vehicle_id)
            self._vehicles[vehicle_id] = vehicle
        vehicle.transport = self._service.get_transport_by_type(transport_type)
        vehicle.route = vehicle.transport.get_route_by_number(route_number)
        vehicle.marks.append(mark)

    def get_vehicles(self, transport_type, route_number):
        return self._vehicles.get_by_transport_and_route(transport_type, route_number)

    def get_forecast(self, transport_type, station_id):
        return self._forecast_calculator.get_forecast(transport_type, station_id)

    def cleanup(self):
        logger.info("Cleaning up datastore...")
        self._remove_outdated_vehicles()
        self._remove_unnecessary_vehicle_marks()

    def _remove_outdated_vehicles(self):
        time = datetime.utcnow() - self.VEHICLE_OUTDATE_INTERVAL
        logger.info("Removing vehicles last updated before %s...", time)
        count = 0
        for vehicle_id, vehicle in dict(self._vehicles).items():
            if vehicle.last_mark.datetime < time:
                del self._vehicles[vehicle_id]
                count += 1
        logger.info("Removed %s vehicles", count)

    def _remove_unnecessary_vehicle_marks(self):
        logger.info("Removing unnecessary vehicle marks...")
        count = 0
        for vehicle in self._vehicles.values():
            marks = vehicle.marks[-self.MAX_MARK_COUNT:]
            count += len(vehicle.marks) - len(marks)
            vehicle.marks = marks
        logger.info("Removed %s marks", count)

class VehicleCollection(dict):

    def get_by_transport_and_route(self, transport, route):
        return filter(lambda vehicle: vehicle.transport == transport and vehicle.route == route, self.values())
