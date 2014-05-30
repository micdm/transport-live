package com.micdm.transportlive.data;

public class ForecastVehicle {

    public final Route route;
    public final int arrivalTime;
    public final boolean isLowFloor;

    public ForecastVehicle(Route route, int arrivalTime, boolean isLowFloor) {
        this.route = route;
        this.arrivalTime = arrivalTime;
        this.isLowFloor = isLowFloor;
    }
}
