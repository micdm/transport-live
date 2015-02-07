from datetime import datetime
from logging import getLogger

from tornado.options import options

from transportlive.forecast.forecast_calculator import ForecastCalculator
from transportlive.misc.low_floor_vehicles_builder import LowFloorVehiclesBuilder
from transportlive.misc.service_builder import ServiceBuilder
from transportlive.misc.station_finder import StationFinder
from transportlive.models import Vehicle

logger = getLogger(__name__)

class DataStore:

    MAX_MARK_COUNT = 30

    def __init__(self):
        self._service = ServiceBuilder().build()
        self._low_floor_vehicles = LowFloorVehiclesBuilder().build()
        self._last_vehicle_updates = {}
        self._vehicles = {}
        self._forecast_calculator = ForecastCalculator(self._service)
        self._station_finder = StationFinder(self._service)
        self._on_update_vehicle = None
        self._on_remove_vehicle = None

    def set_callbacks(self, on_update_vehicle, on_remove_vehicle):
        self._on_update_vehicle = on_update_vehicle
        self._on_remove_vehicle = on_remove_vehicle

    def update_vehicle(self, info):
        last_update = self._last_vehicle_updates.get(info.vehicle_id)
        if last_update is not None and info.datetime_created < last_update.datetime_created:
            logger.warning('Incoming vehicle info is outdated (%s vs %s), skipping...', last_update.datetime_created,
                           info.datetime_created)
            return
        self._last_vehicle_updates[info.vehicle_id] = info
        vehicle = self._get_vehicle(info)
        if self._on_update_vehicle:
            self._on_update_vehicle(vehicle)
        self._forecast_calculator.update_vehicle(vehicle)

    def _get_vehicle(self, info):
        vehicle = self._vehicles.get(info.vehicle_id)
        if not vehicle:
            vehicle = Vehicle(info.vehicle_id, info.number, self._is_low_floor(info.transport_type, info.number))
            self._vehicles[info.vehicle_id] = vehicle
        vehicle.transport = self._service.get_transport_by_type(info.transport_type)
        vehicle.route = vehicle.transport.get_route_by_number(info.route_number)
        vehicle.marks.append(info.mark)
        return vehicle

    def _is_low_floor(self, transport_type, number):
        return transport_type in self._low_floor_vehicles and number in self._low_floor_vehicles[transport_type]

    def get_vehicles(self, transport_type, route_number):
        transport = self._service.get_transport_by_type(transport_type)
        route = transport.get_route_by_number(route_number)
        return list(filter(lambda vehicle: vehicle.transport == transport and vehicle.route == route, self._vehicles.values()))

    def get_forecast(self, transport_type, station_id):
        return self._forecast_calculator.get_forecast(transport_type, station_id)

    def get_nearest_stations(self, latitude, longitude):
        return self._station_finder.find(latitude, longitude)

    def cleanup(self):
        logger.info("Cleaning up datastore...")
        self._remove_outdated_vehicles()
        self._remove_unnecessary_vehicle_marks()

    def _remove_outdated_vehicles(self):
        time = datetime.utcnow() - options.VEHICLE_OUTDATE_INTERVAL
        logger.debug("Removing vehicles last updated before %s...", time)
        count = 0
        for vehicle_id, vehicle in dict(self._vehicles).items():
            if vehicle.last_mark.datetime_created < time:
                self._forecast_calculator.remove_vehicle(vehicle)
                if self._on_remove_vehicle:
                    self._on_remove_vehicle(vehicle)
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
