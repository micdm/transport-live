# coding=utf-8

import unittest

from transportlive.misc.service_builder import ServiceBuilder

class ServiceBuilderTest(unittest.TestCase):

    def setUp(self):
        self._builder = ServiceBuilder()

    def test_build(self):
        self._builder.build()
