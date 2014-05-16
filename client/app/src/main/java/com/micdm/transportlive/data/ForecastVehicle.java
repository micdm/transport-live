package com.micdm.transportlive.data;

public class ForecastVehicle {

    public final Transport transport;
    public final Route route;
    public final int arrivalTime;

    public ForecastVehicle(Transport transport, Route route, int arrivalTime) {
        this.transport = transport;
        this.route = route;
        this.arrivalTime = arrivalTime;
    }
}
