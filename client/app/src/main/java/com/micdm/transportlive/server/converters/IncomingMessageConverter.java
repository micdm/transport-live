package com.micdm.transportlive.server.converters;

import com.micdm.transportlive.server.messages.Message;
import com.micdm.transportlive.server.messages.incoming.ForecastMessage;
import com.micdm.transportlive.server.messages.incoming.NearestStationsMessage;
import com.micdm.transportlive.server.messages.incoming.VehicleMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class IncomingMessageConverter {

    private static class MessageType {

        public static final int VEHICLE = 0;
        public static final int FORECAST = 1;
        public static final int NEAREST_STATIONS = 2;
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
                case MessageType.NEAREST_STATIONS:
                    return getNearestStationsMessage(json);
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
        int transportId = json.getInt("transport_id");
        int stationId = json.getInt("station_id");
        List<ForecastMessage.Vehicle> vehicles = getForecastMessageVehicles(json.getJSONArray("vehicles"));
        return new ForecastMessage(transportId, stationId, vehicles);
    }

    private List<ForecastMessage.Vehicle> getForecastMessageVehicles(JSONArray vehiclesJson) throws JSONException {
        List<ForecastMessage.Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < vehiclesJson.length(); i += 1) {
            JSONObject vehicleJson = vehiclesJson.getJSONObject(i);
            String number = vehicleJson.getString("number");
            int routeNumber = vehicleJson.getInt("route_number");
            int arrivalTime = vehicleJson.getInt("arrival_time");
            boolean isLowFloor = vehicleJson.getBoolean("is_low_floor");
            vehicles.add(new ForecastMessage.Vehicle(number, routeNumber, arrivalTime, isLowFloor));
        }
        return vehicles;
    }

    private NearestStationsMessage getNearestStationsMessage(JSONObject json) throws JSONException {
        JSONArray stationsJson = json.getJSONArray("stations");
        List<NearestStationsMessage.Station> stations = new ArrayList<>();
        for (int i = 0; i < stationsJson.length(); i += 1) {
            JSONObject stationJson = stationsJson.getJSONObject(i);
            int transportId = stationJson.getInt("transport_id");
            int stationId = stationJson.getInt("station_id");
            stations.add(new NearestStationsMessage.Station(transportId, stationId));
        }
        return new NearestStationsMessage(stations);
    }
}
