package com.micdm.transportlive.data;

public class VehicleInfo {

    public Transport transport;
    public Route route;
    public Direction direction;
    public Vehicle vehicle;

    public VehicleInfo(Transport transport, Route route, Direction direction, Vehicle vehicle) {
        this.transport = transport;
        this.route = route;
        this.direction = direction;
        this.vehicle = vehicle;
    }
}
