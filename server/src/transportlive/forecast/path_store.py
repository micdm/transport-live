# coding=utf-8

import os.path

from tornado.escape import json_decode
from tornado.options import options

class PathStore(object):

    PATH_DATA_FILE = os.path.join(options.DATA_ROOT, "service.json")

    def __init__(self):
        self._paths = None

    def load(self):
        self._paths = json_decode(open(self.PATH_DATA_FILE).read())

    def get(self, transport, route):
        if transport not in self._paths:
            raise PathError("cannot find path for transport %s"%transport)
        if route not in self._paths[transport]:
            raise PathError("cannot find path for transport %s and route %s"%(transport, route))
        return self._paths[transport][route]

class PathError(Exception):
    pass
