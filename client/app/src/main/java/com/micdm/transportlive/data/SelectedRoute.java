package com.micdm.transportlive.data;

public class SelectedRoute {

    private final int transportId;
    private final int routeNumber;

    public SelectedRoute(int transportId, int routeNumber) {
        this.transportId = transportId;
        this.routeNumber = routeNumber;
    }

    public int getTransportId() {
        return transportId;
    }

    public int getRouteNumber() {
        return routeNumber;
    }
}
