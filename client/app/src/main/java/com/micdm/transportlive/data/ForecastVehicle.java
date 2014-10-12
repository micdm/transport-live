package com.micdm.transportlive.data;

public class ForecastVehicle {

    private final String number;
    private final int routeNumber;
    private final int arrivalTime;
    private final boolean isLowFloor;

    public ForecastVehicle(String number, int routeNumber, int arrivalTime, boolean isLowFloor) {
        this.number = number;
        this.routeNumber = routeNumber;
        this.arrivalTime = arrivalTime;
        this.isLowFloor = isLowFloor;
    }

    public String getNumber() {
        return number;
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
