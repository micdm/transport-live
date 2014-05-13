# coding=utf-8

from decimal import Decimal
import unittest

from transportlive.forecast.forecast_calculator import DistanceCalculator
from transportlive.misc.dataproviders import use_dataproviders, dataprovider

@use_dataproviders
class DistanceCalculatorTest(unittest.TestCase):

    def setUp(self):
        self._calculator = DistanceCalculator()

    def test_get_path_length(self):
        path = ((Decimal(0), Decimal(0)), (Decimal(2), Decimal(0)), (Decimal(2), Decimal(2)), (Decimal(0), Decimal(2)))
        point = path[-1]
        result = self._calculator._get_path_length(path, point)
        self.assertEqual(result, 8)

    @dataprovider("provider_get_projection")
    def test_get_projection(self, begin, end, point, projection):
        result = self._calculator._get_projection(begin, end, point)
        self.assertEqual(result, projection)

    @staticmethod
    def provider_get_projection():
        return (
            ((Decimal(0), Decimal(0)), (Decimal(0), Decimal(2)), (Decimal(2), Decimal(2)), (Decimal(0), Decimal(2))),
            ((Decimal(0), Decimal(0)), (Decimal(2), Decimal(0)), (Decimal(2), Decimal(2)), (Decimal(2), Decimal(0))),
            ((Decimal(0), Decimal(0)), (Decimal(2), Decimal(2)), (Decimal(2), Decimal(0)), (Decimal(1), Decimal(1)))
        )
