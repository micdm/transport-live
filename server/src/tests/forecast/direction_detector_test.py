# coding=utf-8

from decimal import Decimal
import unittest

from transportlive.forecast.direction_detector import DirectionDetector
from transportlive.models.point import Point

class DirectionDetectorTest(unittest.TestCase):

    def setUp(self):
        self._detector = DirectionDetector()

    def test_get_direction(self):
        point1, point2 = Point(Decimal("56.509104"), Decimal("84.981986")), Point(Decimal("56.511612"), Decimal("84.975506"))
        self.assertEqual(self._detector.get_direction(0, 4, point1, point2).id, 8)
        self.assertEqual(self._detector.get_direction(0, 4, point2, point1).id, 9)
