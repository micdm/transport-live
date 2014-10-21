from decimal import Decimal
import unittest

from transportlive.misc.station_finder import StationFinder
from transportlive.models import Service, Transport, Station, Coords

class ServiceBuilderTest(unittest.TestCase):

    def setUp(self):
        self._finder = StationFinder(self._get_test_service())

    def _get_test_service(self):
        service = Service()
        transport1 = Transport(Transport.TYPE_TROLLEYBUS)
        transport1.stations.append(Station(1, Coords(Decimal("85.000"), Decimal("55.001")), "station1"))
        transport1.stations.append(Station(2, Coords(Decimal("85.001"), Decimal("55.000")), "station2"))
        transport1.stations.append(Station(3, Coords(Decimal("85.000"), Decimal("54.000")), "station3"))
        service.transports.append(transport1)
        transport2 = Transport(Transport.TYPE_TRAM)
        transport2.stations.append(Station(4, Coords(Decimal("85.001"), Decimal("55.001")), "station4"))
        transport2.stations.append(Station(5, Coords(Decimal("84.000"), Decimal("55.000")), "station5"))
        service.transports.append(transport2)
        return service

    def test_find(self):
        results = self._finder.find(Decimal("85.000"), Decimal("55.000"))
        self.assertEqual(len(results), StationFinder.MAX_RESULT_COUNT)
        stations = [result["station"] for result in results]
        for station in stations:
            self.assertIn(station.id, (1, 2, 4))
