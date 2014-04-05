package com.micdm.transportlive.data;

public class ForecastVehicle {

    public Transport transport;
    public Route route;
    public int arrivalTime;

    public ForecastVehicle(Transport transport, Route route, int arrivalTime) {
        this.transport = transport;
        this.route = route;
        this.arrivalTime = arrivalTime;
    }
}
