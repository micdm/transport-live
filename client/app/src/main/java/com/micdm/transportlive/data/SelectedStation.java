package com.micdm.transportlive.data;

public class SelectedStation {

    private final int transportId;
    private final int routeNumber;
    private final int directionId;
    private final int stationId;
    private final boolean isFavourite;

    public SelectedStation(int transportId, int routeNumber, int directionId, int stationId) {
        this(transportId, routeNumber, directionId, stationId, false);
    }

    public SelectedStation(int transportId, int routeNumber, int directionId, int stationId, boolean isFavourite) {
        this.transportId = transportId;
        this.routeNumber = routeNumber;
        this.directionId = directionId;
        this.stationId = stationId;
        this.isFavourite = isFavourite;
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

    public boolean isFavourite() {
        return isFavourite;
    }
}
