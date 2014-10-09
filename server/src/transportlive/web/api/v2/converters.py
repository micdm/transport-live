# coding=utf-8

from tornado.escape import json_decode, json_encode

import transportlive.web.api.v2.messages as messages

class IncomingMessageConverter(object):

    MESSAGE_TYPE_GREETING = 0
    MESSAGE_TYPE_SELECT_ROUTE = 1
    MESSAGE_TYPE_UNSELECT_ROUTE = 2
    MESSAGE_TYPE_SELECT_STATION = 3
    MESSAGE_TYPE_UNSELECT_STATION = 4

    def convert(self, text):
        params = self._unserialize(text)
        message_type = params["type"]
        if message_type == self.MESSAGE_TYPE_GREETING:
            return messages.GreetingMessage(params["version"])
        if message_type == self.MESSAGE_TYPE_SELECT_ROUTE:
            return messages.SelectRouteMessage(int(params["transport_id"]), int(params["route_number"]))
        if message_type == self.MESSAGE_TYPE_UNSELECT_ROUTE:
            return messages.UnselectRouteMessage(int(params["transport_id"]), int(params["route_number"]))
        if message_type == self.MESSAGE_TYPE_SELECT_STATION:
            return messages.SelectStationMessage(int(params["transport_id"]), int(params["station_id"]))
        if message_type == self.MESSAGE_TYPE_UNSELECT_STATION:
            return messages.UnselectStationMessage(int(params["transport_id"]), int(params["station_id"]))
        raise Exception("unknown message type {}".format(message_type))

    def _unserialize(self, text):
        return json_decode(text)

class OutcomingMessageConverter(object):

    MESSAGE_TYPE_VEHICLE = 0

    def convert(self, message):
        params = {"type": self._get_message_type(message)}
        if isinstance(message, messages.VehicleMessage):
            params.update({
                "number": message.number,
                "transport_id": message.transport_id,
                "route_number": message.route_number,
                "latitude": str(message.latitude),
                "longitude": str(message.longitude),
                "course": message.course
            })
        return self._serialize(params)

    def _get_message_type(self, message):
        if isinstance(message, messages.VehicleMessage):
            return self.MESSAGE_TYPE_VEHICLE
        raise Exception("unknown message type %s", message)

    def _serialize(self, params):
        return json_encode(params)
