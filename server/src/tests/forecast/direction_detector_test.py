# coding=utf-8

import unittest

from transportlive.forecast.direction_detector import DirectionDetector

class DirectionDetectorTest(unittest.TestCase):

    def setUp(self):
        self._detector = DirectionDetector()

    def test_is_straight(self):
        point1, point2 = (56509104, 84981986), (56511612, 84975506)
        self.assertTrue(self._detector.is_straight(0, 4, point1, point2))
        self.assertFalse(self._detector.is_straight(0, 4, point2, point1))
