from decimal import Decimal
import os.path
import xml.etree.ElementTree as etree

from tornado.options import options

from transportlive.models import Service, Station, Transport, Route, Direction, Coords

class ServiceBuilder:

    DATA_FILE = os.path.join(options.DATA_ROOT, "service.xml")

    def build(self):
        xml = etree.parse(self.DATA_FILE)
        service = self._build_service(xml.getroot())
        self._add_stations_to_directions(service)
        return service

    def _build_service(self, node):
        service = Service()
        for child in node.findall("./transports/transport"):
            service.transports.append(self._build_transport(child))
        return service

    def _build_transport(self, node):
        transport = Transport(int(node.attrib["type"]))
        for child in node.findall("./stations/station"):
            transport.stations.append(self._build_station(child))
        for child in node.findall("./routes/route"):
            transport.routes.append(self._build_route(child))
        return transport

    def _build_station(self, node):
        coords = Coords(Decimal(node.attrib["lat"]), Decimal(node.attrib["lon"]))
        return Station(int(node.attrib["id"]), coords, node.attrib["name"])

    def _build_route(self, node):
        route = Route(int(node.attrib["number"]))
        for child in node.findall("./directions/direction"):
            route.directions.append(self._build_direction(child))
        return route

    def _build_direction(self, node):
        direction = Direction(int(node.attrib["id"]))
        for child in node.findall("./stations/station"):
            direction.stations.append(int(child.attrib["id"]))
        for child in node.findall("./points/point"):
            direction.points.append(self._build_point(child))
        return direction

    def _build_point(self, node):
        return Coords(Decimal(node.attrib["lat"]), Decimal(node.attrib["lon"]))

    def _add_stations_to_directions(self, service):
        for transport in service.transports:
            for route in transport.routes:
                for direction in route.directions:
                    direction.stations = map(transport.get_station_by_id, direction.stations)
