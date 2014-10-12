from transportlive.models import Transport, Mark, Coords

class VehicleBuilder:

    def build(self, packet):
        transport, route = self._get_transport_and_route(packet)
        if transport is None or route is None:
            return None
        mark = Mark(packet.datetime, Coords(packet.latitude, packet.longitude), self._get_normalized_speed(packet.speed), packet.course)
        is_on_line = self._is_on_line(packet)
        return VehicleInfo(packet.imei, self._get_number(packet), transport, route, mark, is_on_line)

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

    def _get_number(self, packet):
        return packet.params.get("gosnum")

    def _is_on_line(self, packet):
        params = packet.params
        return params.get("break") is None and params.get("dinner") is None and params.get("nonprocessrun") is None

class VehicleInfo:

    def __init__(self, vehicle_id, number, transport_type, route_number, mark, is_on_line):
        self.vehicle_id = vehicle_id
        self.number = number
        self.transport_type = transport_type
        self.route_number = route_number
        self.mark = mark
        self.is_on_line = is_on_line

class IncorrectPacketError(Exception):
    pass
