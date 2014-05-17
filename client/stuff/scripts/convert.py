#!/usr/bin/python
# coding=utf-8

from decimal import Decimal
import json
import re
import sys
import xml.etree.ElementTree as etree

xml = etree.parse(sys.argv[1]).getroot()
IS_EXTENDED_MODE = (sys.argv[2] == "server")

def _get_transports(xml):
    transports = []
    for node in xml.findall(".//transport"):
        transports.append({
            "id": int(node.attrib["id"]),
            "stations": _get_all_stations(node),
            "routes": _get_routes(node)
        })
    return transports

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
            "name": _get_normalized_station_name(node.attrib["name"])
        })
    stations.sort(key=lambda item: item["id"])
    return stations

def _is_station_exist(stations, station_id):
    for station in stations:
        if station["id"] == station_id:
            return True
    return False

def _get_normalized_station_name(name):
    name = re.sub("(.)\.([^ ])", "\\1. \\2", name)
    name = re.sub("(.)\\((.)", "\\1 (\\2", name)
    name = name.replace(u"Главпочтамп", u"Главпочтамт")
    name = name.replace(u"Сурова", u"Суворова")
    name = name.replace(u"Агенство", u"Агентство")
    name = name.replace(u"Киевкая", u"Киевская")
    return name

def _get_routes(xml):
    routes = []
    for node in xml.findall(".//route"):
        routes.append({
            "number": int(node.attrib["number"]),
            "directions": _get_directions(node)
        })
    return routes

def _get_directions(xml):
    directions = []
    for i, node in enumerate(xml.findall(".//direction")):
        directions.append({
            "id": int(node.attrib["id"]),
            "stations": _get_stations(node),
            "points": _get_points(node)
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
    prev_point = None
    for node in xml.findall(".//point"):
        point = {
            "lat": Decimal(node.attrib["lat"]) / 1000000,
            "lon": Decimal(node.attrib["lon"]) / 1000000
        }
        if not prev_point or point["lat"] != prev_point["lat"] or point["lon"] != prev_point["lon"]:
            points.append(point)
        prev_point = point
    return points

def _build_xml(data):
    node = etree.Element("service")
    _build_transports_xml(data["transports"], node)
    return node

def _build_transports_xml(transports, parent):
    node = etree.SubElement(parent, "transports")
    for transport in transports:
        _build_transport_xml(transport, node)

def _build_transport_xml(transport, parent):
    node = etree.SubElement(parent, "transport", {
        "id": str(_get_transport_id(transport["id"]))
    })
    _build_all_stations_xml(transport["stations"], node)
    _build_routes_xml(transport["routes"], node)

def _get_transport_id(id):
    if id == 2:
        return 0
    if id == 3:
        return 1

def _build_all_stations_xml(stations, parent):
    node = etree.SubElement(parent, "stations")
    for station in stations:
        _build_station_data_xml(station, node)

def _build_station_data_xml(station, parent):
    info = {
        "id": str(station["id"]),
        "name": station["name"]
    }
    if IS_EXTENDED_MODE:
        info.update({
            "lat": str(station["lat"]),
            "lon": str(station["lon"])
        })
    etree.SubElement(parent, "station", info)

def _build_routes_xml(routes, parent):
    node = etree.SubElement(parent, "routes")
    for route in routes:
        _build_route_xml(route, node)

def _build_route_xml(route, parent):
    node = etree.SubElement(parent, "route", {
        "number": str(route["number"])
    })
    _build_directions_xml(route["directions"], node)

def _build_directions_xml(directions, parent):
    node = etree.SubElement(parent, "directions")
    for direction in directions:
        _build_direction_xml(direction, node)

def _build_direction_xml(direction, parent):
    node = etree.SubElement(parent, "direction", {
        "id": str(direction["id"])
    })
    _build_stations_xml(direction["stations"], node)
    if IS_EXTENDED_MODE:
        _build_points_xml(direction["points"], node)

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
    "transports": _get_transports(xml)
}
xml = _build_xml(data)
print '<?xml version="1.0" encoding="UTF-8"?>'
print etree.tostring(xml, "utf-8")
