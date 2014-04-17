# coding=utf-8

from logging import getLogger
import logging.config

from tornado.ioloop import IOLoop
from tornado.options import define, options

from transportlive.data.server import start_data_server
import settings

logger = getLogger(__name__)

def _setup_settings():
    for name, value in settings.__dict__.items():
        if name.isupper():
            define(name, value)

def _setup_logger():
    logging.config.dictConfig(options.LOGGING)

_setup_settings()
_setup_logger()
start_data_server(options)
IOLoop.instance().start()
