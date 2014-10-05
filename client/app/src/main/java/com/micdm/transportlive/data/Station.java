package com.micdm.transportlive.data;

public class Station {

    private final int id;
    private final String name;

    public Station(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
