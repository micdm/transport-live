package com.micdm.transportlive.server2.converters;

import com.micdm.transportlive.server2.messages.Message;
import com.micdm.transportlive.server2.messages.outcoming.GreetingMessage;
import com.micdm.transportlive.server2.messages.outcoming.SelectRouteMessage;
import com.micdm.transportlive.server2.messages.outcoming.UnselectRouteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class OutcomingMessageConverter {

    private static class MessageType {

        private static final int GREETING = 0;
        private static final int SELECT_ROUTE = 1;
        private static final int UNSELECT_ROUTE = 2;
    }

    public String convert(Message message) {
        try {
            JSONObject json = new JSONObject();
            int type = getMessageType(message);
            json.put("type", type);
            switch (type) {
                case MessageType.GREETING:
                    buildJsonForGreetingMessage((GreetingMessage) message, json);
                    break;
                case MessageType.SELECT_ROUTE:
                    buildJsonForSelectRouteMessage((SelectRouteMessage) message, json);
                    break;
                case MessageType.UNSELECT_ROUTE:
                    buildJsonForUnselectRouteMessage((UnselectRouteMessage) message, json);
                    break;
            }
            return serialize(json);
        } catch (JSONException e) {
            throw new RuntimeException("cannot convert message to JSON");
        }
    }

    private int getMessageType(Message message) {
        if (message instanceof GreetingMessage) {
            return MessageType.GREETING;
        }
        if (message instanceof SelectRouteMessage) {
            return MessageType.SELECT_ROUTE;
        }
        if (message instanceof UnselectRouteMessage) {
            return MessageType.UNSELECT_ROUTE;
        }
        throw new RuntimeException("unknown message type");
    }

    private void buildJsonForGreetingMessage(GreetingMessage message, JSONObject json) throws JSONException {
        json.put("version", message.getVersion());
    }

    private void buildJsonForSelectRouteMessage(SelectRouteMessage message, JSONObject json) throws JSONException {
        json.put("transport_id", message.getTransportId());
        json.put("route_number", message.getRouteNumber());
    }

    private void buildJsonForUnselectRouteMessage(UnselectRouteMessage message, JSONObject json) throws JSONException {
        json.put("transport_id", message.getTransportId());
        json.put("route_number", message.getRouteNumber());
    }

    private String serialize(JSONObject json) {
        return json.toString();
    }
}
