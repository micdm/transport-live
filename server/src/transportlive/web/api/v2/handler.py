from tornado.websocket import WebSocketHandler

class ApiHandler(WebSocketHandler):

    # TODO: запустить таймер неактивности

    def initialize(self, client_manager):
        self._client_manager = client_manager

    def check_origin(self, origin):
        return True

    def on_message(self, message):
        self._client_manager.on_data(self, message)

    def on_close(self):
        self._client_manager.on_close(self)
