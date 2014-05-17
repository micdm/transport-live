package com.micdm.transportlive.data;

public class ForecastVehicle {

    public final Route route;
    public final int arrivalTime;

    public ForecastVehicle(Route route, int arrivalTime) {
        this.route = route;
        this.arrivalTime = arrivalTime;
    }
}
