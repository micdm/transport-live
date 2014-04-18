# coding=utf-8

from datetime import datetime
from decimal import Decimal
import re

from transportlive.data.packets import LoginPacket, LoginAnswerPacket, PingPacket, PingAnswerPacket, DataPacket, DataAnswerPacket

class PacketSerializer(object):

    def serialize(self, packet):
        return "#%s#%s\r\n"%(self._get_type(packet), self._get_status(packet))

    def _get_type(self, packet):
        if isinstance(packet, LoginAnswerPacket):
            return "AL"
        if isinstance(packet, PingAnswerPacket):
            return "AP"
        if isinstance(packet, DataAnswerPacket):
            return "AD"

    def _get_status(self, packet):
        if isinstance(packet, LoginAnswerPacket):
            if packet.status == LoginAnswerPacket.STATUS_OK:
                return "1"
        if isinstance(packet, PingAnswerPacket):
            return ""
        if isinstance(packet, DataAnswerPacket):
            if packet.status == DataAnswerPacket.STATUS_OK:
                return "1"

class PacketUnserializer(object):

    def unserialize(self, string):
        matched = re.match("#([A-Z]+)#([^\r]*?)\r\n", string)
        if not matched:
            return None
        return len(matched.group(0)), matched.group(1), matched.group(2).split(";")

class PacketBuilder(object):

    VALUE_NOT_AVAILABLE = "NA"

    def build(self, packet_type, parts):
        if packet_type == "L":
            return self._build_login_packet(parts)
        if packet_type == "P":
            return self._build_ping_packet(parts)
        if packet_type == "D":
            return self._build_data_packet(parts)

    def _build_login_packet(self, parts):
        return LoginPacket(self._get_login(parts[0]), self._get_password(parts[1]))

    def _get_login(self, login_string):
        return login_string

    def _get_password(self, password_string):
        return None if password_string == self.VALUE_NOT_AVAILABLE else password_string

    def _build_ping_packet(self, parts):
        return PingPacket()

    def _build_data_packet(self, parts):
        return DataPacket(self._get_id(parts[0]), self._get_datetime(parts[1], parts[2]), self._get_coordinate(parts[3]),
                          self._get_coordinate(parts[5]), self._get_speed(parts[7]), self._get_course(parts[8]))

    def _get_id(self, id_string):
        return id_string

    def _get_datetime(self, date_string, time_string):
        if date_string == self.VALUE_NOT_AVAILABLE or time_string == self.VALUE_NOT_AVAILABLE:
            return None
        return datetime.strptime("%s %s"%(date_string, time_string), "%d%m%y %H%M%S")

    def _get_coordinate(self, nmea_string):
        if nmea_string == self.VALUE_NOT_AVAILABLE:
            return None
        nmea_value = Decimal(nmea_string)
        return (nmea_value / 100).quantize(Decimal(1)) + (nmea_value % 100) / 60

    def _get_speed(self, speed_string):
        return None if speed_string == self.VALUE_NOT_AVAILABLE else int(speed_string)

    def _get_course(self, course_string):
        return None if course_string == self.VALUE_NOT_AVAILABLE else int(course_string)
