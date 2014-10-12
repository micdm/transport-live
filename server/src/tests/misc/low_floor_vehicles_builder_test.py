import unittest

from transportlive.misc.low_floor_vehicles_builder import LowFloorVehiclesBuilder
from transportlive.models import Transport

class LowFloorVehiclesBuilderTest(unittest.TestCase):

    def setUp(self):
        self._builder = LowFloorVehiclesBuilder()

    def test_build(self):
        result = self._builder.build()
        self.assertIsInstance(result, dict)
        self.assertNotEqual(len(result), 0)
        self.assertNotEqual(len(result[Transport.TYPE_TROLLEYBUS]), 0)
