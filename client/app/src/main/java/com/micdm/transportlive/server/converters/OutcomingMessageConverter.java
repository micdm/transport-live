package com.micdm.transportlive.server.converters;

import com.micdm.transportlive.server.messages.Message;
import com.micdm.transportlive.server.messages.outcoming.GreetingMessage;
import com.micdm.transportlive.server.messages.outcoming.LoadNearestStationsMessage;
import com.micdm.transportlive.server.messages.outcoming.SelectRouteMessage;
import com.micdm.transportlive.server.messages.outcoming.SelectStationMessage;
import com.micdm.transportlive.server.messages.outcoming.UnselectRouteMessage;
import com.micdm.transportlive.server.messages.outcoming.UnselectStationMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class OutcomingMessageConverter {

    private static class MessageType {

        private static final int GREETING = 0;
        private static final int SELECT_ROUTE = 1;
        private static final int UNSELECT_ROUTE = 2;
        private static final int SELECT_STATION = 3;
        private static final int UNSELECT_STATION = 4;
        private static final int LOAD_NEAREST_STATIONS = 5;
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
                case MessageType.SELECT_STATION:
                    buildJsonForSelectStationMessage((SelectStationMessage) message, json);
                    break;
                case MessageType.UNSELECT_STATION:
                    buildJsonForUnselectStationMessage((UnselectStationMessage) message, json);
                    break;
                case MessageType.LOAD_NEAREST_STATIONS:
                    buildJsonForLoadNearestStationsMessage((LoadNearestStationsMessage) message, json);
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
        if (message instanceof SelectStationMessage) {
            return MessageType.SELECT_STATION;
        }
        if (message instanceof UnselectStationMessage) {
            return MessageType.UNSELECT_STATION;
        }
        if (message instanceof LoadNearestStationsMessage) {
            return MessageType.LOAD_NEAREST_STATIONS;
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

    private void buildJsonForSelectStationMessage(SelectStationMessage message, JSONObject json) throws JSONException {
        json.put("transport_id", message.getTransportId());
        json.put("station_id", message.getStationId());
    }

    private void buildJsonForUnselectStationMessage(UnselectStationMessage message, JSONObject json) throws JSONException {
        json.put("transport_id", message.getTransportId());
        json.put("station_id", message.getStationId());
    }

    private void buildJsonForLoadNearestStationsMessage(LoadNearestStationsMessage message, JSONObject json) throws JSONException {
        json.put("latitude", message.getLatitude().toString());
        json.put("longitude", message.getLongitude().toString());
    }

    private String serialize(JSONObject json) {
        return json.toString();
    }
}
