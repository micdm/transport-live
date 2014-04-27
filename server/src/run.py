# coding=utf-8

from datetime import timedelta
import logging.config

from tornado.ioloop import IOLoop
from tornado.options import define, options

from transportlive.datastore import DataStore
from transportlive.data.server import start_data_server
from transportlive.web.server import start_web_server
import settings

ioloop = IOLoop.instance()

def _setup_settings():
    for name, value in settings.__dict__.items():
        if name.isupper():
            define(name, value)

def _setup_logger():
    logging.config.dictConfig(options.LOGGING)

def _setup_datastore():
    datastore = DataStore()
    interval = timedelta(minutes=5)
    def _cleanup():
        datastore.cleanup()
        ioloop.add_timeout(interval, _cleanup)
    ioloop.add_timeout(interval, _cleanup)
    return datastore

_setup_settings()
_setup_logger()
datastore = _setup_datastore()
start_data_server(datastore)
start_web_server(datastore)
ioloop.start()
