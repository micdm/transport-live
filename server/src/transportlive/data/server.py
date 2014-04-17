# coding=utf-8

from logging import getLogger
import re

from tornado.tcpserver import TCPServer

from transportlive.data.packet_builder import PacketBuilder

logger = getLogger(__name__)

class DataServer(TCPServer):

    MAX_BUFFER_SIZE = 1024

    def __init__(self):
        super(DataServer, self).__init__()
        self._packet_builder = PacketBuilder()
        self._stream = None
        self._buffer = ""

    def handle_stream(self, stream, address):
        logger.info("New connection from %s:%s", *address)
        if self._stream:
            logger.warning("Cannot handle connection, there is another one active already")
        else:
            self._stream = stream
            stream.read_until_close(lambda data: self._on_close_stream(), self._on_stream_data)

    def _on_stream_data(self, data):
        logger.debug("New data on connection of length %s", len(data))
        self._buffer += data
        self._parse_packets()
        if len(self._buffer) > self.MAX_BUFFER_SIZE:
            logger.warning("Buffer grows too much, closing connection...")
            self._stream.close()

    def _on_close_stream(self):
        logger.info("Connection closed")
        self._stream = None

    def _parse_packets(self):
        while True:
            packet = self._parse_packet()
            if not packet:
                break
            logger.info("New packet of type %s available", packet.__class__.__name__)

    def _parse_packet(self):
        matched = re.match("#([A-Z]+)#([^\r]*?)\r\n", self._buffer)
        if not matched:
            return None
        length = len(matched.group(0))
        self._buffer = self._buffer[length:]
        return self._packet_builder.build(matched.group(1), matched.group(2).split(";"))

def start_data_server(options):
    host = options.DATA_SERVER["host"]
    port = options.DATA_SERVER["port"]
    logger.info("Starting data server on %s:%s...", host, port)
    server = DataServer()
    server.listen(port, host)
