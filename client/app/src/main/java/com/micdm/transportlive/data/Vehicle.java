package com.micdm.transportlive.data;

import java.math.BigDecimal;

public class Vehicle {

    private final String number;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final int course;

    public Vehicle(String number, BigDecimal latitude, BigDecimal longitude, int course) {
        this.number = number;
        this.latitude = latitude;
        this.longitude = longitude;
        this.course = course;
    }

    public String getNumber() {
        return number;
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
