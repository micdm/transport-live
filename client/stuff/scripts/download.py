#!/usr/bin/python
# coding=utf-8

import httplib
import json
import urllib
import xml.etree.ElementTree as etree

FIRST_BACKEND = {"host": "bus62.ru", "url": "/tomsk/php/%s.php"}
SECOND_BACKEND = {"host": "83.222.106.126", "url": "/bus/common/map6/%s.php"}

def _send_request(backend, method, params=None):
    if not params:
        params = {}
    params["city"] = "tomsk"
    connection = httplib.HTTPConnection(backend["host"])
    connection.request("GET", "%s?%s"%(backend["url"]%method, urllib.urlencode(params)))
    response = connection.getresponse()
    body = response.read()
    connection.close()
    return body

def _response_to_xml(response):
    return etree.XML(response, etree.XMLParser(encoding="utf-8"))

def _load_all():
    transports = _load_transports()
    _load_routes(transports)
    for transport in transports:
        for route in transport["routes"]:
            _load_stations(transport, route)
            _load_points(transport, route)
    return transports

def _load_transports():
    response = json.loads(_send_request(FIRST_BACKEND, "searchAllRouteTypes"))
    return [{
        "id": int(item["typeId"]),
        "name": item["typeName"],
        "code": item["typeShName"]
    } for item in response]

def _load_routes(transports):
    response = json.loads(_send_request(FIRST_BACKEND, "searchAllRoutes"))
    for transport in transports:
        serialized = response[transport["name"]]
        unserialized = map(lambda item: item.split(";"), filter(len, serialized.split("@ROUTE=")))
        routes = []
        for chunks in unserialized:
            routes.append({
                "number": int(chunks[2]),
                "directions": [
                    {"id": int(chunks[6])},
                    {"id": int(chunks[7])}
                ]
            })
        transport["routes"] = routes

def _get_transport_drive_type(transport):
    if transport["id"] == 2:
        return 0
    if transport["id"] == 3:
        return 1

def _load_stations(transport, route):
    response = _response_to_xml(_send_request(SECOND_BACKEND, "getRouteStations", {
        "type": _get_transport_drive_type(transport),
        "id1": route["directions"][0]["id"],
        "id2": route["directions"][1]["id"]
    }))
    directions = iter(route["directions"])
    direction = None
    for item in response.findall("station"):
        station = {
            "id": int(item.attrib["id"]),
            "name": item.attrib["name"],
            "lat": int(item.attrib["lat0"]),
            "lon": int(item.attrib["lon0"])
        }
        if "end" in item.attrib:
            if direction is None:
                direction = directions.next()
                direction["stations"] = [station]
            else:
                direction["stations"].append(station)
                direction = None
        else:
            direction["stations"].append(station)

def _load_points(transport, route):
    response = _response_to_xml(_send_request(SECOND_BACKEND, "getSubRoutePolyline", {
        "type": _get_transport_drive_type(transport),
        "id1": route["directions"][0]["id"],
        "id2": route["directions"][1]["id"]
    }))
    direction = route["directions"][0]
    prev_point = None
    for i, item in enumerate(response.findall("node")):
        point = {
            "lat": int(item.attrib["lat"]),
            "lon": int(item.attrib["lon"])
        }
        if prev_point and point["lat"] == prev_point["lat"] and point["lon"] == prev_point["lon"] and i != 1:
            direction = route["directions"][1]
        if "points" not in direction:
            direction["points"] = []
        direction["points"].append(point)
        prev_point = point

def _build_xml(transports):
    node = etree.Element("service")
    for transport in transports:
        _build_transport_xml(transport, node)
    return node

def _build_transport_xml(transport, parent):
    node = etree.SubElement(parent, "transport", {
        "id": str(transport["id"]),
        "code": transport["code"],
        "type": _get_transport_type(transport["name"])
    })
    for route in transport["routes"]:
        _build_route_xml(route, node)

def _get_transport_type(name):
    if name == u"Автобус":
        return "BUS"
    if name == u"Троллейбус":
        return "TROLLEYBUS"
    if name == u"Трамвай":
        return "TRAM"
    if name == u"Маршрутное такси":
        return "TAXI"

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
    _build_points_xml(direction["points"], node)

def _build_stations_xml(stations, parent):
    node = etree.SubElement(parent, "stations")
    for station in stations:
        _build_station_xml(station, node)

def _build_station_xml(station, parent):
    etree.SubElement(parent, "station", {
        "id": str(station["id"]),
        "name": station["name"],
        "lat": str(station["lat"]),
        "lon": str(station["lon"])
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

transports = _load_all()
xml = _build_xml(transports)
print '<?xml version="1.0" encoding="UTF-8"?>'
print etree.tostring(xml, "utf-8")
