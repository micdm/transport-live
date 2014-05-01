# coding=utf-8

from decimal import Decimal
import unittest

from transportlive.data.packet_utils import PacketBuilder

class PacketBuilderTest(unittest.TestCase):

    def setUp(self):
        self._builder = PacketBuilder()

    def test_get_coordinate(self):
        self.assertEqual(self._builder._get_coordinate("5630.3099"), Decimal("56.505165"))
