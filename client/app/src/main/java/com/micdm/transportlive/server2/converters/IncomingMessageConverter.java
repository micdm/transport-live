package com.micdm.transportlive.server2.converters;

import com.micdm.transportlive.server2.messages.Message;
import com.micdm.transportlive.server2.messages.incoming.ForecastMessage;
import com.micdm.transportlive.server2.messages.incoming.VehicleMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class IncomingMessageConverter {

    private static class MessageType {

        public static final int VEHICLE = 0;
        public static final int FORECAST = 1;
    }

    public Message convert(String message) {
        try {
            JSONObject json = unserialize(message);
            int type = json.getInt("type");
            switch (type) {
                case MessageType.VEHICLE:
                    return getVehicleMessage(json);
                case MessageType.FORECAST:
                    return getForecastMessage(json);
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
        String number = json.getString("number");
        int transportId = json.getInt("transport_id");
        int routeNumber = json.getInt("route_number");
        BigDecimal latitude = new BigDecimal(json.getString("latitude"));
        BigDecimal longitude = new BigDecimal(json.getString("longitude"));
        int course = json.getInt("course");
        return new VehicleMessage(number, transportId, routeNumber, latitude, longitude, course);
    }

    private ForecastMessage getForecastMessage(JSONObject json) throws JSONException {
        String number = json.getString("number");
        int transportId = json.getInt("transport_id");
        int routeNumber = json.getInt("route_number");
        int stationId = json.getInt("station_id");
        int arrivalTime = json.getInt("arrival_time");
        boolean isLowFloor = json.getBoolean("is_low_floor");
        return new ForecastMessage(number, transportId, routeNumber, stationId, arrivalTime, isLowFloor);
    }
}
