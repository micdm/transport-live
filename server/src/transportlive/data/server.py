# coding=utf-8

from logging import getLogger

from tornado.options import options
from tornado.tcpserver import TCPServer

from transportlive.data.packet_utils import PacketSerializer, PacketUnserializer, PacketBuilder
from transportlive.data.packets import LoginPacket, LoginAnswerPacket, PingPacket, PingAnswerPacket, DataPacket, DataAnswerPacket
from transportlive.vehicle_builder import VehicleBuilder

logger = getLogger(__name__)

class DataServer(TCPServer):

    MAX_BUFFER_SIZE = 1024

    def __init__(self, datastore):
        super(DataServer, self).__init__()
        self._packet_serializer = PacketSerializer()
        self._packet_unserializer = PacketUnserializer()
        self._packet_builder = PacketBuilder()
        self._vehicle_builder = VehicleBuilder()
        self._stream = None
        self._buffer = ""
        self._session = None
        self._datastore = datastore

    def handle_stream(self, stream, address):
        # TODO: разрешать повторное подключение
        # TODO: запускать таймер и обрывать соединение, если не пришел пакет логина
        logger.info("New connection from %s:%s", *address)
        if self._stream:
            logger.warning("Cannot handle connection, there is another one active already")
        else:
            self._stream = stream
            stream.read_until_close(lambda data: self._on_close_stream(), self._on_stream_data)

    def _on_stream_data(self, data):
        logger.debug("New data on connection of length %s", len(data))
        logger.debug(data)
        self._buffer += data
        self._parse_packets()
        if len(self._buffer) > self.MAX_BUFFER_SIZE:
            logger.warning("Buffer grows too much, closing connection...")
            self._stream.close()

    def _on_close_stream(self):
        logger.info("Connection closed")
        self._cleanup()

    def _parse_packets(self):
        while True:
            packet = self._parse_packet()
            if not packet:
                break
            if isinstance(packet, LoginPacket):
                self._handle_login_packet(packet)
            if isinstance(packet, PingPacket):
                self._handle_ping_packet(packet)
            if isinstance(packet, DataPacket):
                self._handle_data_packet(packet)

    def _parse_packet(self):
        if not self._buffer:
            return None
        packet_info = self._packet_unserializer.unserialize(self._buffer)
        if not packet_info:
            return None
        length, packet_type, parts = packet_info
        self._buffer = self._buffer[length:]
        return self._packet_builder.build(packet_type, parts)

    def _handle_login_packet(self, packet):
        logger.info("Login packet received")
        if self._session:
            logger.warning("Session already started, closing connection...")
            self._stream.close()
        else:
            if packet.login != options.DATA_SERVER["login"] or packet.password != options.DATA_SERVER["password"]:
                logger.warning("Wrong login (%s) or password, closing connection...", packet.login)
                self._stream.close()
            else:
                logger.info("Starting new session...")
                answer_packet = LoginAnswerPacket(LoginAnswerPacket.STATUS_OK)
                self._stream.write(self._packet_serializer.serialize(answer_packet))
                self._session = Session(packet.login)

    def _handle_ping_packet(self, packet):
        logger.info("Ping packet received")
        answer_packet = PingAnswerPacket()
        self._stream.write(self._packet_serializer.serialize(answer_packet))

    def _handle_data_packet(self, packet):
        logger.info("Data packet received")
        if not self._session:
            logger.warning("No session started, closing connection...")
            self._stream.close()
        else:
            answer_packet = DataAnswerPacket(DataAnswerPacket.STATUS_OK)
            self._stream.write(self._packet_serializer.serialize(answer_packet))
            vehicle = self._vehicle_builder.build(packet)
            if not vehicle:
                logger.info("Cannot build vehicle, skipping packet...")
            else:
                self._datastore.add_vehicle(vehicle)

    def _cleanup(self):
        self._stream = None
        self._buffer = ""
        self._session = None

class Session(object):

    def __init__(self, login):
        self.login = login

def start_data_server(datastore):
    host, port = options.DATA_SERVER["host"], options.DATA_SERVER["port"]
    logger.info("Starting data server on %s:%s...", host, port)
    server = DataServer(datastore)
    server.listen(port, host)
