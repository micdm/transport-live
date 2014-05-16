package com.micdm.transportlive.data;

import java.math.BigDecimal;

public class Vehicle {

    public final String number;
    public final BigDecimal latitude;
    public final BigDecimal longitude;
    public final int course;

    public Vehicle(String number, BigDecimal latitude, BigDecimal longitude, int course) {
        this.number = number;
        this.latitude = latitude;
        this.longitude = longitude;
        this.course = course;
    }
}
