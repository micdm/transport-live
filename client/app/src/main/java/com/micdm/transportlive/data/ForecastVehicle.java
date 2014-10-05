package com.micdm.transportlive.data;

public class ForecastVehicle {

    private final int routeNumber;
    private final int arrivalTime;
    private final boolean isLowFloor;

    public ForecastVehicle(int routeNumber, int arrivalTime, boolean isLowFloor) {
        this.routeNumber = routeNumber;
        this.arrivalTime = arrivalTime;
        this.isLowFloor = isLowFloor;
    }

    public int getRouteNumber() {
        return routeNumber;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public boolean isLowFloor() {
        return isLowFloor;
    }
}
