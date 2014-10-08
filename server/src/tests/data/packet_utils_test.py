# coding=utf-8

from decimal import Decimal
import unittest

from transportlive.data.packet_utils import PacketBuilder
from transportlive.misc.dataproviders import use_dataproviders, dataprovider

@use_dataproviders
class PacketBuilderTest(unittest.TestCase):

    def setUp(self):
        self._builder = PacketBuilder()

    @dataprovider("provider_get_coordinate")
    def test_get_coordinate(self, nmea_string, coordinate):
        result = self._builder._get_coordinate(nmea_string)
        self.assertEqual(result, coordinate)

    @staticmethod
    def provider_get_coordinate():
        return (
            ("5630.3099", Decimal("56.505165")),
            ("8456.9972", Decimal("84.949953"))
        )
