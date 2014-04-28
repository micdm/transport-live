# coding=utf-8

import os.path

from tornado.escape import json_decode
from tornado.options import options

class PathStore(object):

    PATH_DATA_FILE = os.path.join(options.DATA_ROOT, "service.json")

    def __init__(self):
        self._paths = None

    def _load(self):
        data = json_decode(open(self.PATH_DATA_FILE).read())
        return self._load_transports(data["transports"])

    def _load_transports(self, data):
        return dict((item["type"], self._load_routes(item["routes"])) for item in data)

    def _load_routes(self, data):
        return dict((item["number"], self._load_points(item["points"])) for item in data)

    def _load_points(self, data):
        return list((item["lat"], item["lon"]) for item in data)

    def get(self, transport, route):
        if self._paths is None:
            self._paths = self._load()
        if transport not in self._paths:
            raise PathError("cannot find path for transport %s"%transport)
        if route not in self._paths[transport]:
            raise PathError("cannot find path for transport %s and route %s"%(transport, route))
        return self._paths[transport][route]

class PathError(Exception):
    pass
