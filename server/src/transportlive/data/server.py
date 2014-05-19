# coding=utf-8

from datetime import timedelta
from logging import getLogger

from tornado.ioloop import IOLoop
from tornado.options import options
from tornado.tcpserver import TCPServer

from transportlive.data.packet_utils import PacketSerializer, PacketUnserializer, PacketBuilder
from transportlive.data.packets import LoginPacket, LoginAnswerPacket, PingPacket, PingAnswerPacket, DataPacket, DataAnswerPacket
from transportlive.data.vehicle_builder import VehicleBuilder

logger = getLogger(__name__)

class DataServer(TCPServer):

    PACKET_COUNTER_INTERVAL = timedelta(minutes=10)

    def __init__(self, datastore):
        super(DataServer, self).__init__()
        self._vehicle_builder = VehicleBuilder()
        self._datastore = datastore
        self._stream_count = 0
        self._packet_count = 0

    def start_packet_counter(self):
        ioloop = IOLoop.instance()
        def _report_and_restart():
            logger.info("Received %s packets in last %s", self._packet_count, self.PACKET_COUNTER_INTERVAL)
            self._packet_count = 0
            ioloop.add_timeout(self.PACKET_COUNTER_INTERVAL, _report_and_restart)
        ioloop.add_timeout(self.PACKET_COUNTER_INTERVAL, )

    def handle_stream(self, stream, address):
        logger.info("New connection from %s:%s", *address)
        self._stream_count += 1
        StreamHandler(self._stream_count, stream, self._handle_data_packet).run()

    def _handle_data_packet(self, packet):
        self._packet_count += 1
        vehicle_info = self._vehicle_builder.build(packet)
        if not vehicle_info:
            logger.debug("Cannot build vehicle, skipping packet...")
        else:
            self._datastore.add_vehicle(*vehicle_info)

class StreamHandler(object):

    MAX_BUFFER_SIZE = 1024
    CLOSE_STREAM_INTERVAL = timedelta(seconds=30)

    def __init__(self, handler_id, stream, on_data_packet):
        self._packet_serializer = PacketSerializer()
        self._packet_unserializer = PacketUnserializer()
        self._packet_builder = PacketBuilder()
        self._id = handler_id
        self._stream = stream
        self._buffer = ""
        self._session = None
        self._on_data_packet = on_data_packet
        self._close_timeout = None

    def run(self):
        self._stream.read_until_close(lambda data: self._on_close_stream(), self._on_stream_data)
        self._close_timeout = IOLoop.instance().add_timeout(self.CLOSE_STREAM_INTERVAL, self._close_unathorized_stream)

    def _close_unathorized_stream(self):
        logger.warning("Stream %s unathorized too long, closing stream...", self._id)
        self._stream.close()

    def _on_stream_data(self, data):
        logger.debug("New data on stream %s of length %s:\r\n%s", self._id, len(data), data)
        self._buffer += data
        self._parse_packets()
        if len(self._buffer) > self.MAX_BUFFER_SIZE:
            logger.warning("Buffer grows too much, closing stream...")
            self._stream.close()

    def _parse_packets(self):
        while True:
            packet = self._parse_packet()
            if not packet:
                break
            self._handle_packet(packet)

    def _parse_packet(self):
        if not self._buffer:
            return None
        packet_info = self._packet_unserializer.unserialize(self._buffer)
        if not packet_info:
            return None
        length, packet_type, parts = packet_info
        self._buffer = self._buffer[length:]
        return self._packet_builder.build(packet_type, parts)

    def _handle_packet(self, packet):
        if isinstance(packet, LoginPacket):
            self._handle_login_packet(packet)
        if isinstance(packet, PingPacket):
            self._handle_ping_packet(packet)
        if isinstance(packet, DataPacket):
            self._handle_data_packet(packet)

    def _handle_login_packet(self, packet):
        logger.debug("Login packet received")
        if self._session:
            logger.warning("Session already started, closing stream...")
            self._stream.close()
        else:
            if packet.login != options.DATA_SERVER["login"] or packet.password != options.DATA_SERVER["password"]:
                logger.warning("Wrong login (%s) or password, closing stream...", packet.login)
                self._stream.close()
            else:
                logger.info("Starting new session...")
                answer_packet = LoginAnswerPacket(LoginAnswerPacket.STATUS_OK)
                self._stream.write(self._packet_serializer.serialize(answer_packet))
                self._session = Session(packet.login)
                IOLoop.instance().remove_timeout(self._close_timeout)

    def _handle_ping_packet(self, packet):
        logger.debug("Ping packet received")
        answer_packet = PingAnswerPacket()
        self._stream.write(self._packet_serializer.serialize(answer_packet))

    def _handle_data_packet(self, packet):
        logger.debug("Data packet received")
        if not self._session:
            logger.warning("No session started, closing stream...")
            self._stream.close()
        else:
            answer_packet = DataAnswerPacket(DataAnswerPacket.STATUS_OK)
            self._stream.write(self._packet_serializer.serialize(answer_packet))
            self._on_data_packet(packet)

    def _on_close_stream(self):
        logger.debug("Stream %s closed", self._id)

class Session(object):

    def __init__(self, login):
        self.login = login

def start_data_server(datastore):
    host, port = options.DATA_SERVER["host"], options.DATA_SERVER["port"]
    logger.info("Starting data server on %s:%s...", host, port)
    server = DataServer(datastore)
    server.start_packet_counter()
    server.listen(port, host)
