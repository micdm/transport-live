package com.micdm.transportlive.data;

import java.math.BigDecimal;
import java.util.Date;

public class Vehicle {

    public String number;
    public BigDecimal latitude;
    public BigDecimal longitude;
    public int course;
    public Date lastUpdate;

    public Vehicle(String number, BigDecimal latitude, BigDecimal longitude, int course, Date lastUpdate) {
        this.number = number;
        this.latitude = latitude;
        this.longitude = longitude;
        this.course = course;
        this.lastUpdate = lastUpdate;
    }
}
