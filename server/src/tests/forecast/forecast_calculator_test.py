# coding=utf-8

from decimal import Decimal
import unittest

from transportlive.forecast.forecast_calculator import _get_projection, _is_point_inside_segment
from transportlive.misc.dataproviders import use_dataproviders, dataprovider
from transportlive.models import Coords

@use_dataproviders
class MiscTest(unittest.TestCase):

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
