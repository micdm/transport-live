package com.micdm.transportlive.data;

public class VehicleInfo {

    public final Transport transport;
    public final Route route;
    public final Vehicle vehicle;

    public VehicleInfo(Transport transport, Route route, Vehicle vehicle) {
        this.transport = transport;
        this.route = route;
        this.vehicle = vehicle;
    }
}
