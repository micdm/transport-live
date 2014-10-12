from decimal import Decimal
import unittest

from transportlive.forecast.forecast_calculator import _get_nearest_point_index, _get_projection, _is_point_inside_segment, _get_distance_in_meters
from transportlive.misc.dataproviders import use_dataproviders, dataprovider
from transportlive.models import Coords

@use_dataproviders
class MiscTest(unittest.TestCase):

    @dataprovider("provider_get_nearest_point_index")
    def test_get_nearest_point_index(self, begin, end, point, index):
        result = _get_nearest_point_index([begin, end], point)
        self.assertEqual(index, result)

    @staticmethod
    def provider_get_nearest_point_index():
        return (
            (Coords(Decimal("56.455632"), Decimal("84.95132")), Coords(Decimal("56.455811"), Decimal("84.95117")), Coords(Decimal("56.455561"), Decimal("84.951369")), None),
         )

    @dataprovider("provider_get_projection")
    def test_get_projection(self, begin, end, point, projection):
        result = _get_projection(begin, end, point)
        self.assertEqual(result.latitude, projection.latitude)
        self.assertEqual(result.longitude, projection.longitude)

    @staticmethod
    def provider_get_projection():
        return (
            (Coords(Decimal(0), Decimal(0)), Coords(Decimal(0), Decimal(2)), Coords(Decimal(2), Decimal(2)), Coords(Decimal(0), Decimal(2))),
            (Coords(Decimal(0), Decimal(0)), Coords(Decimal(2), Decimal(0)), Coords(Decimal(2), Decimal(2)), Coords(Decimal(2), Decimal(0))),
            (Coords(Decimal(0), Decimal(0)), Coords(Decimal(2), Decimal(2)), Coords(Decimal(2), Decimal(0)), Coords(Decimal(1), Decimal(1)))
         )

    @dataprovider("provider_is_point_inside_segment")
    def test_is_point_inside_segment_if_true(self, begin, end, point):
        result = _is_point_inside_segment(begin, end, point)
        self.assertTrue(result)

    @staticmethod
    def provider_is_point_inside_segment():
        return (
            (Coords(Decimal(0), Decimal(0)), Coords(Decimal(2), Decimal(2)), Coords(Decimal(1), Decimal(1))),
            (Coords(Decimal(2), Decimal(0)), Coords(Decimal(0), Decimal(2)), Coords(Decimal(1), Decimal(1))),
            (Coords(Decimal(0), Decimal(2)), Coords(Decimal(2), Decimal(0)), Coords(Decimal(1), Decimal(1))),
            (Coords(Decimal(2), Decimal(2)), Coords(Decimal(0), Decimal(0)), Coords(Decimal(1), Decimal(1))),
         )

    @dataprovider("provider_get_distance_in_meters")
    def test_get_distance_in_meters(self, point1, point2, distance):
        result = _get_distance_in_meters(point1, point2)
        self.assertEqual(result, distance)

    @staticmethod
    def provider_get_distance_in_meters():
        return (
            (Coords(Decimal("56.492777"), Decimal("84.948283")), Coords(Decimal("56.492777"), Decimal("84.948283")), 0),
         )
