package com.micdm.transportlive.data;

public class Station {

    public int id;
    public String name;
    public int latitude;
    public int longitude;

    public Station(int id, String name, int latitude, int longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
