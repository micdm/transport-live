# coding=utf-8

from decimal import Decimal
import os.path
import xml.etree.ElementTree as etree

from tornado.options import options
from transportlive.models.direction import Direction
from transportlive.models.point import Point

from transportlive.models.route import Route
from transportlive.models.service import Service
from transportlive.models.station import Station
from transportlive.models.transport import Transport

class ServiceBuilder(object):

    DATA_FILE = os.path.join(options.DATA_ROOT, "service.xml")

    def build(self):
        xml = etree.parse(self.DATA_FILE)
        service = self._build_service(xml.getroot())
        self._add_stations_to_directions(service)
        return service

    def _build_service(self, node):
        service = Service()
        for child in node.findall("./stations/station"):
            service.stations.append(self._build_station(child))
        for child in node.findall("./transports/transport"):
            service.transports.append(self._build_transport(child))
        return service

    def _build_station(self, node):
        return Station(int(node.attrib["id"]), Decimal(node.attrib["lat"]), Decimal(node.attrib["lon"]), node.attrib["name"])

    def _build_transport(self, node):
        transport = Transport(int(node.attrib["type"]))
        for child in node.findall("./routes/route"):
            transport.routes.append(self._build_route(child))
        return transport

    def _build_route(self, node):
        route = Route(int(node.attrib["number"]))
        for child in node.findall("./directions/direction"):
            route.directions.append(self._build_direction(child))
        for child in node.findall("./points/point"):
            route.points.append(self._build_point(child))
        return route

    def _build_direction(self, node):
        direction = Direction(int(node.attrib["id"]))
        for child in node.findall("./stations/station"):
            direction.stations.append(int(child.attrib["id"]))
        return direction

    def _build_point(self, node):
        return Point(Decimal(node.attrib["lat"]), Decimal(node.attrib["lon"]))

    def _add_stations_to_directions(self, service):
        for transport in service.transports:
            for route in transport.routes:
                for direction in route.directions:
                    direction.stations = map(service.get_station_by_id, direction.stations)
