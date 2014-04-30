# coding=utf-8

from logging import getLogger

from tornado.options import options
from tornado.web import Application

from transportlive.web.api.v1 import VehicleHandler, ForecastHandler

logger = getLogger(__name__)

def _get_app(datastore):
    return Application([
        (r"/api/v1/vehicles", VehicleHandler, {"datastore": datastore}),
        (r"/api/v1/forecasts", ForecastHandler, {"datastore": datastore}),
    ], debug=options.DEBUG)

def start_web_server(datastore):
    host, port = options.WEB_SERVER["host"], options.WEB_SERVER["port"]
    logger.info("Starting web server on %s:%s...", host, port)
    app = _get_app(datastore)
    app.listen(port, host)
