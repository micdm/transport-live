import os.path
import xml.etree.ElementTree as etree

from tornado.options import options

class LowFloorVehiclesBuilder:

    DATA_FILE = os.path.join(options.DATA_ROOT, "low_floor_vehicles.xml")

    def build(self):
        xml = etree.parse(self.DATA_FILE)
        return self._build_transports(xml.getroot())

    def _build_transports(self, node):
        transports = {}
        for child in node.findall("./transport"):
            transport_type = int(child.attrib["type"])
            transports[transport_type] = self._build_vehicles(child)
        return transports

    def _build_vehicles(self, node):
        return [child.attrib["number"] for child in node.findall("./vehicles/vehicle")]
