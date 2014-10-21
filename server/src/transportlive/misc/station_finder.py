from decimal import Decimal
from logging import getLogger

logger = getLogger(__name__)

class StationFinder:

    MAX_DELTA = Decimal("0.001")
    MAX_RESULT_COUNT = 3

    def __init__(self, service):
        self._service = service

    def find(self, latitude, longitude):
        stations = []
        for transport in self._service.transports:
            for station in transport.stations:
                lat_delta = abs(station.coords.latitude - latitude)
                lon_delta = abs(station.coords.longitude - longitude)
                if lat_delta <= self.MAX_DELTA and lon_delta <= self.MAX_DELTA:
                    stations.append({
                        "lat_delta": lat_delta,
                        "lon_delta": lon_delta,
                        "transport": transport,
                        "station": station
                    })
        return self._get_closest(stations)

    def _get_closest(self, stations: list):
        stations.sort(key=lambda item: item["lat_delta"] ** 2 + item["lon_delta"] ** 2)
        return stations[:self.MAX_RESULT_COUNT]
