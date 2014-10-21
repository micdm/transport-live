from decimal import Decimal, ROUND_FLOOR

class StationFinder:

    MAX_DELTA = Decimal("0.001")
    MAX_RESULT_COUNT = 3

    def __init__(self, service):
        self._regions = self._get_regions(service)

    def _get_regions(self, service):
        regions = {}
        for transport in service.transports:
            for station in transport.stations:
                region_latitude, region_longitude = self._get_region_coords(station.coords.latitude, station.coords.longitude)
                key = self._get_region_key(region_latitude, region_longitude)
                region = regions.get(key)
                if not region:
                    region = []
                    regions[key] = region
                region.append((transport, station))
        return regions

    def find(self, latitude: Decimal, longitude: Decimal):
        stations = []
        region_latitude, region_longitude = self._get_region_coords(latitude, longitude)
        for i in range(-1, 2):
            for j in range(-1, 2):
                key = self._get_region_key(region_latitude + self.MAX_DELTA * i, region_longitude + self.MAX_DELTA * j)
                region = self._regions.get(key)
                if region:
                    stations.extend(self._get_nearest(region, latitude, longitude))
        return self._get_closest(stations)

    def _get_region_coords(self, latitude, longitude):
        return latitude.quantize(self.MAX_DELTA, rounding=ROUND_FLOOR), longitude.quantize(self.MAX_DELTA, rounding=ROUND_FLOOR)

    def _get_region_key(self, latitude, longitude):
        return "{}-{}".format(latitude, longitude)

    def _get_nearest(self, region, latitude, longitude):
        for transport, station in region:
            lat_delta = abs(station.coords.latitude - latitude)
            lon_delta = abs(station.coords.longitude - longitude)
            if lat_delta <= self.MAX_DELTA and lon_delta <= self.MAX_DELTA:
                yield {
                    "lat_delta": lat_delta,
                    "lon_delta": lon_delta,
                    "transport": transport,
                    "station": station
                }

    def _get_closest(self, stations: list):
        stations.sort(key=lambda item: item["lat_delta"] ** 2 + item["lon_delta"] ** 2)
        return stations[:self.MAX_RESULT_COUNT]
