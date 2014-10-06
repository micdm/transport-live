package com.micdm.transportlive.server2.converters;

import com.micdm.transportlive.server2.messages.Message;
import com.micdm.transportlive.server2.messages.incoming.VehicleMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class IncomingMessageConverter {

    private static class MessageType {

        public static final int VEHICLE = 0;
    }

    public Message convert(String message) {
        try {
            JSONObject json = unserialize(message);
            int type = json.getInt("type");
            switch (type) {
                case MessageType.VEHICLE:
                    return getVehicleMessage(json);
                default:
                    throw new RuntimeException(String.format("unknown message type %s", type));
            }
        } catch (JSONException e) {
            throw new RuntimeException("cannot convert JSON to message");
        }
    }

    private JSONObject unserialize(String message) throws JSONException {
        return new JSONObject(message);
    }

    private VehicleMessage getVehicleMessage(JSONObject json) throws JSONException {
        int transportId = json.getInt("transport_id");
        int routeNumber = json.getInt("route_number");
        String number = json.getString("number");
        BigDecimal latitude = new BigDecimal(json.getString("latitude"));
        BigDecimal longitude = new BigDecimal(json.getString("longitude"));
        int course = json.getInt("course");
        return new VehicleMessage(transportId, routeNumber, number, latitude, longitude, course);
    }
}
