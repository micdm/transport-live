#!/usr/bin/python

from decimal import Decimal
import json
import sys
import xml.etree.ElementTree as etree

xml = etree.parse(sys.argv[1]).getroot()

def _get_all_stations(xml):
    stations = []
    for node in xml.findall(".//station"):
        station_id = int(node.attrib["id"])
        if _is_station_exist(stations, station_id):
            continue
        stations.append({
            "id": station_id,
            "lat": Decimal(node.attrib["lat"]) / 1000000,
            "lon": Decimal(node.attrib["lon"]) / 1000000,
            "name": node.attrib["name"]
        })
    stations.sort(key=lambda item: item["id"])
    return stations

def _is_station_exist(stations, station_id):
    for station in stations:
        if station["id"] == station_id:
            return True
    return False

def _get_transports(xml):
    transports = []
    for node in xml.findall(".//transport"):
        transports.append({
            "type": int(node.attrib["id"]),
            "routes": _get_routes(node)
        })
    return transports

def _get_routes(xml):
    routes = []
    for node in xml.findall(".//route"):
        routes.append({
            "number": int(node.attrib["number"]),
            "directions": _get_directions(node),
            "points": _get_points(node)
        })
    return routes

def _get_directions(xml):
    directions = []
    for node in xml.findall(".//direction"):
        directions.append({
            "id": int(node.attrib["id"]),
            "stations": _get_stations(node)
        })
    return directions

def _get_stations(xml):
    stations = []
    for node in xml.findall(".//station"):
        station_id = int(node.attrib["id"])
        stations.append(station_id)
    return stations

def _get_points(xml):
    points = []
    for node in xml.findall(".//point"):
        points.append({
            "lat": Decimal(node.attrib["lat"]) / 1000000,
            "lon": Decimal(node.attrib["lon"]) / 1000000
        })
    return points

def _build_xml(data):
    node = etree.Element("service")
    _build_all_stations_xml(data["stations"], node)
    _build_transports_xml(data["transports"], node)
    return node

def _build_all_stations_xml(stations, parent):
    node = etree.SubElement(parent, "stations")
    for station in stations:
        _build_station_data_xml(station, node)

def _build_station_data_xml(station, parent):
    etree.SubElement(parent, "station", {
        "id": str(station["id"]),
        "lat": str(station["lat"]),
        "lon": str(station["lon"]),
        "name": station["name"]
    })

def _build_transports_xml(transports, parent):
    node = etree.SubElement(parent, "transports")
    for transport in transports:
        _build_transport_xml(transport, node)

def _build_transport_xml(transport, parent):
    node = etree.SubElement(parent, "transport", {
        "type": str(_get_transport_type(transport["type"]))
    })
    _build_routes_xml(transport["routes"], node)

def _get_transport_type(transport_type):
    if transport_type == 2:
        return 0
    if transport_type == 3:
        return 1

def _build_routes_xml(routes, parent):
    node = etree.SubElement(parent, "routes")
    for route in routes:
        _build_route_xml(route, node)

def _build_route_xml(route, parent):
    node = etree.SubElement(parent, "route", {
        "number": str(route["number"])
    })
    _build_directions_xml(route["directions"], node)
    _build_points_xml(route["points"], node)

def _build_directions_xml(directions, parent):
    node = etree.SubElement(parent, "directions")
    for direction in directions:
        _build_direction_xml(direction, node)

def _build_direction_xml(direction, parent):
    node = etree.SubElement(parent, "direction", {
        "id": str(direction["id"])
    })
    _build_stations_xml(direction["stations"], node)

def _build_stations_xml(stations, parent):
    node = etree.SubElement(parent, "stations")
    for station in stations:
        _build_station_id_xml(station, node)

def _build_station_id_xml(station_id, parent):
    etree.SubElement(parent, "station", {
        "id": str(station_id),
    })

def _build_points_xml(points, parent):
    node = etree.SubElement(parent, "points")
    for point in points:
        _build_point_xml(point, node)

def _build_point_xml(point, parent):
    etree.SubElement(parent, "point", {
        "lat": str(point["lat"]),
        "lon": str(point["lon"])
    })

data = {
    "stations": _get_all_stations(xml),
    "transports": _get_transports(xml)
}
xml = _build_xml(data)
print '<?xml version="1.0" encoding="UTF-8" ?>'
print etree.tostring(xml, "utf-8")
