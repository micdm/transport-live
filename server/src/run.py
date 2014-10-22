from datetime import timedelta
from logging import getLogger
import logging
import logging.config
import sys
import unittest

from tornado.ioloop import IOLoop
from tornado.options import define, options

import settings

def _setup_settings():
    for name, value in settings.__dict__.items():
        if name.isupper():
            define(name, value)

def _setup_logger():
    logging.config.dictConfig(options.LOGGING)

_setup_settings()
_setup_logger()
logger = getLogger(__name__)
ioloop = IOLoop.instance()

from transportlive.datastore import DataStore
from transportlive.data.server import start_data_server
from transportlive.web.server import start_web_server

def _run_main():
    datastore = _setup_datastore()
    start_data_server(datastore)
    start_web_server(datastore)
    ioloop.start()

def _setup_datastore():
    datastore = DataStore()
    interval = timedelta(minutes=5)
    def _cleanup():
        datastore.cleanup()
        ioloop.add_timeout(interval, _cleanup)
    ioloop.add_timeout(interval, _cleanup)
    return datastore

def _run_tests():
    logging.disable(logging.CRITICAL)
    loader = unittest.TestLoader()
    tests = loader.discover(options.TEST_ROOT, "*test.py")
    unittest.TextTestRunner(verbosity=2).run(tests)

def _get_command_function(command):
    if command == "main":
        return _run_main
    if command == "tests":
        return _run_tests
    return None

if len(sys.argv) < 2:
    logger.error("Please specify command")
else:
    function = _get_command_function(sys.argv[1])
    if function is None:
        logger.error("Please specify correct command")
    else:
        function()
