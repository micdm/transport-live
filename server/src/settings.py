# coding=utf-8

DEBUG = False

LOGGING = {
    "version": 1,
    "disable_existing_loggers": False,
    "formatters": {
        "simple": {
            "format": "%(asctime)s [%(levelname)s] %(message)s"
        }
    },
    "handlers": {
        "default": {
            "level": "DEBUG",
            "class": "logging.StreamHandler",
            "formatter": "simple"
        }
    },
    "loggers": {
        "": {
            "handlers": ["default"],
            "level": "DEBUG",
            "propagate": False
        }
    }
}

DATA_SERVER = {
    "host": "localhost",
    "port": 8000,
    "login": None,
    "password": None
}

WEB_SERVER = {
    "host": "localhost",
    "port": 8001
}

try:
    from settings_local import *
except ImportError:
    pass
