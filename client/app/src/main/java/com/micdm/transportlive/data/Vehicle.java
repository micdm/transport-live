package com.micdm.transportlive.data;

import java.math.BigDecimal;

public class Vehicle {

    private final String number;
    private int transportId;
    private int routeNumber;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private int course;

    public Vehicle(String number) {
        this(number, 0, 0, null, null, 0);
    }

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

    public void setTransportId(int transportId) {
        this.transportId = transportId;
    }

    public int getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(int routeNumber) {
        this.routeNumber = routeNumber;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }
}
