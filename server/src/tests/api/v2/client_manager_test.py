import unittest

from mox3 import mox

from transportlive.misc.dataproviders import dataprovider, use_dataproviders
from transportlive.models import Forecast, Vehicle
from transportlive.web.api.v2.client_manager import ClientManager

@use_dataproviders
class ClientManagerTest(unittest.TestCase):

    def setUp(self):
        self._mox = mox.Mox()
        self._client_manager = ClientManager(self._mox.CreateMockAnything())

    def tearDown(self):
        self._mox.ResetAll()

    @classmethod
    def _build_forecast(cls, *vehicles):
        forecast = Forecast(None, None)
        forecast.vehicles.extend(vehicles)
        return forecast

    @classmethod
    def _build_vehicle(cls, vehicle_id, arrival_time):
        return {
            "vehicle": Vehicle(vehicle_id, None, None),
            "arrival_time": arrival_time
        }

    @dataprovider("provider_is_forecast_changed_if_changed")
    def test_is_forecast_changed_if_changed(self, forecast, original):
        station = None
        self._client_manager._forecasts[station] = original
        result = self._client_manager._is_forecast_changed(station, forecast)
        self.assertTrue(result)

    @classmethod
    def provider_is_forecast_changed_if_changed(cls):
        return (
            (cls._build_forecast(cls._build_vehicle(0, 0)), None),
            (cls._build_forecast(cls._build_vehicle(0, 0), cls._build_vehicle(1, 0)), cls._build_forecast(cls._build_vehicle(0, 0))),
            (cls._build_forecast(cls._build_vehicle(0, 0)), cls._build_forecast(cls._build_vehicle(0, 0), cls._build_vehicle(1, 0))),
            (cls._build_forecast(cls._build_vehicle(0, 0)), cls._build_forecast(cls._build_vehicle(0, 1))),
        )

    def test_is_forecast_changed_if_not_changed(self):
        station = None
        forecast = self._build_forecast(self._build_vehicle(0, 0))
        self._client_manager._forecasts[station] = forecast
        result = self._client_manager._is_forecast_changed(station, forecast)
        self.assertFalse(result)
