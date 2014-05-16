package com.micdm.transportlive.data;

public class VehicleInfo {

    public Transport transport;
    public Route route;
    public Vehicle vehicle;

    public VehicleInfo(Transport transport, Route route, Vehicle vehicle) {
        this.transport = transport;
        this.route = route;
        this.vehicle = vehicle;
    }
}
