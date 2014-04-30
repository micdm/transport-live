# coding=utf-8

import unittest

from transportlive.forecast.direction_detector import DirectionDetector
from transportlive.models.point import Point

class DirectionDetectorTest(unittest.TestCase):

    def setUp(self):
        self._detector = DirectionDetector()

    def test_get_direction(self):
        point1, point2 = Point(56509104, 84981986), Point(56511612, 84975506)
        self.assertEqual(self._detector.get_direction(0, 4, point1, point2).id, 8)
        self.assertEqual(self._detector.get_direction(0, 4, point2, point1).id, 9)
