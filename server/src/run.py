# coding=utf-8

import logging.config

from tornado.ioloop import IOLoop
from tornado.options import define, options

from transportlive.datastore import DataStore
from transportlive.data.server import start_data_server
from transportlive.web.server import start_web_server
import settings

def _setup_settings():
    for name, value in settings.__dict__.items():
        if name.isupper():
            define(name, value)

def _setup_logger():
    logging.config.dictConfig(options.LOGGING)

_setup_settings()
_setup_logger()
datastore = DataStore()
start_data_server(datastore)
start_web_server(datastore)
IOLoop.instance().start()
