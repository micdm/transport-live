package com.micdm.transportlive.data;

import java.util.List;

public class RoutePopulation {

    private final int transportId;
    private final int routeNumber;
    private final List<Vehicle> vehicles;

    public RoutePopulation(int transportId, int routeNumber, List<Vehicle> vehicles) {
        this.transportId = transportId;
        this.routeNumber = routeNumber;
        this.vehicles = vehicles;
    }

    public int getTransportId() {
        return transportId;
    }

    public int getRouteNumber() {
        return routeNumber;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }
}
