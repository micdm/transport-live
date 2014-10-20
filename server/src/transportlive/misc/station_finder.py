from decimal import Decimal
from logging import getLogger

logger = getLogger(__name__)

class StationFinder:

    MAX_DELTA = Decimal("0.0009")

    def __init__(self, service):
        self._service = service

    def find(self, latitude, longitude):
        stations = []
        for transport in self._service.transports:
            for station in transport.stations:
                if abs(station.coords.latitude - latitude) <= self.MAX_DELTA and abs(station.coords.longitude - longitude) <= self.MAX_DELTA:
                    stations.append((transport, station))
        return stations
