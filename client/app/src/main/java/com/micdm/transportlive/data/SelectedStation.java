package com.micdm.transportlive.data;

public class SelectedStation {

    private final int transportId;
    private final int routeNumber;
    private final int directionId;
    private final int stationId;

    public SelectedStation(int transportId, int routeNumber, int directionId, int stationId) {
        this.transportId = transportId;
        this.routeNumber = routeNumber;
        this.directionId = directionId;
        this.stationId = stationId;
    }

    public int getTransportId() {
        return transportId;
    }

    public int getRouteNumber() {
        return routeNumber;
    }

    public int getDirectionId() {
        return directionId;
    }

    public int getStationId() {
        return stationId;
    }
}
