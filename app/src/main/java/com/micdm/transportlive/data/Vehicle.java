package com.micdm.transportlive.data;

import java.util.Date;

public class Vehicle {

    public String id;
    public String number;
    public int latitude;
    public int longitude;
    public int direction;
    public Date lastUpdate;

    public Vehicle(String id, String number, int latitude, int longitude, int direction, Date lastUpdate) {
        this.id = id;
        this.number = number;
        this.latitude = latitude;
        this.longitude = longitude;
        this.direction = direction;
        this.lastUpdate = lastUpdate;
    }
}
