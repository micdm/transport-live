package com.micdm.transportlive.data;

import java.math.BigDecimal;

public class Vehicle {

    private final String number;
    private final int transportId;
    private final int routeNumber;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final int course;

    public Vehicle(String number, int transportId, int routeNumber, BigDecimal latitude, BigDecimal longitude, int course) {
        this.number = number;
        this.transportId = transportId;
        this.routeNumber = routeNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.course = course;
    }

    public String getNumber() {
        return number;
    }

    public int getTransportId() {
        return transportId;
    }

    public int getRouteNumber() {
        return routeNumber;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public int getCourse() {
        return course;
    }
}
