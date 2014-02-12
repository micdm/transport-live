package com.micdm.transportlive.data;

public class Vehicle {

    public int id;
    public String number;
    public int latitude;
    public int longitude;

    public Vehicle(int id, String number, int latitude, int longitude) {
        this.id = id;
        this.number = number;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
