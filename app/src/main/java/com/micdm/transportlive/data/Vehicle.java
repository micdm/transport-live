package com.micdm.transportlive.data;

import java.util.Date;

public class Vehicle {

    public int id;
    public String number;
    public Point location;
    public int direction;
    public Date lastUpdate;

    public Vehicle(int id, String number, Point location, int direction, Date lastUpdate) {
        this.id = id;
        this.number = number;
        this.location = location;
        this.direction = direction;
        this.lastUpdate = lastUpdate;
    }
}
