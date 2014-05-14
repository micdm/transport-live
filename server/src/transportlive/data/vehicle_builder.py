# coding=utf-8

from transportlive.models import Transport, Mark, Coords

class VehicleBuilder(object):

    def build(self, packet):
        transport, route = self._get_transport_and_route(packet)
        if transport is None or route is None:
            return None
        mark = Mark(packet.datetime, Coords(packet.latitude, packet.longitude), self._get_normalized_speed(packet.speed), packet.course)
        return packet.imei, transport, route, mark

    def _get_transport_and_route(self, packet):
        info = packet.params.get("num")
        if not info:
            return None, None
        parts = info.split(" ")
        if len(parts) != 2:
            raise IncorrectPacketError('incorrect parameter "%s"'%info)
        return self._get_transport(parts[1]), int(parts[0])

    def _get_transport(self, type_string):
        if type_string == "тролл":
            return Transport.TYPE_TROLLEYBUS
        if type_string == "трамв":
            return Transport.TYPE_TRAM
        raise IncorrectPacketError('unknown transport type "%s"'%type_string)

    def _get_normalized_speed(self, speed):
        return float(speed) * 1000 / 3600

class IncorrectPacketError(Exception):
    pass
