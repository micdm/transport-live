# coding=utf-8

from datetime import datetime, timedelta
from logging import getLogger

from transportlive.models.vehicle import Vehicle

logger = getLogger(__name__)

class DataStore(object):

    MAX_MARK_COUNT = 10
    VEHICLE_OUTDATE_INTERVAL = timedelta(minutes=1)

    def __init__(self):
        self._vehicles = {}

    def get_vehicles(self, transport, route):
        return filter(lambda vehicle: vehicle.transport == transport and vehicle.route == route, self._vehicles.values())

    def add_vehicle(self, vehicle_id, transport, route, mark):
        vehicle = self._vehicles.get(vehicle_id)
        if not vehicle:
            vehicle = Vehicle(vehicle_id)
            self._vehicles[vehicle_id] = vehicle
        vehicle.transport = transport
        vehicle.route = route
        vehicle.marks.append(mark)
        vehicle.marks = vehicle.marks[-self.MAX_MARK_COUNT:]

    def cleanup(self):
        logger.info("Cleaning up datastore...")
        self._remove_outdated_vehicles()

    def _remove_outdated_vehicles(self):
        time = datetime.utcnow() - self.VEHICLE_OUTDATE_INTERVAL
        logger.info("Removing vehicles last updated before %s...", time)
        count = 0
        for vehicle_id, vehicle in dict(self._vehicles).items():
            if vehicle.last_mark.datetime < time:
                del self._vehicles[vehicle_id]
                count += 1
        logger.info("Removed %s vehicles", count)
