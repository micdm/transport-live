package com.micdm.transportlive.server2.messages.incoming;

import com.micdm.transportlive.server2.messages.Message;

public class ForecastMessage implements Message {

    private final String number;
    private final int transportId;
    private final int routeNumber;
    private final int stationId;
    private final int arrivalTime;
    private final boolean isLowFloor;

    public ForecastMessage(String number, int transportId, int routeNumber, int stationId, int arrivalTime, boolean isLowFloor) {
        this.number = number;
        this.transportId = transportId;
        this.routeNumber = routeNumber;
        this.stationId = stationId;
        this.arrivalTime = arrivalTime;
        this.isLowFloor = isLowFloor;
    }

    public String getNumber() {
        return number;
    }

    public int getTransportId() {
        return transportId;
    }

    public int getRouteNumber() {
        return routeNumber;
    }

    public int getStationId() {
        return stationId;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public boolean isLowFloor() {
        return isLowFloor;
    }
}
