import os.path

from tornado.options import options

from transportlive.data.packet_utils import PacketUnserializer, PacketBuilder

class TestDataHandler:

    DATA_FILE = os.path.join(options.DATA_ROOT, "test.txt")

    def __init__(self, on_data_packet):
        self._packet_unserializer = PacketUnserializer()
        self._packet_builder = PacketBuilder()
        self._on_data_packet = on_data_packet

    def run(self):
        with open(self.DATA_FILE) as test_data:
            for packet_content in test_data:
                packet = self._get_packet("%s\r\n"%packet_content.strip())
                self._on_data_packet(packet)

    def _get_packet(self, data):
        length, packet_type, parts = self._packet_unserializer.unserialize(data)
        return self._packet_builder.build(packet_type, parts)
