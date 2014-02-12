package com.micdm.transportlive.data;

import java.util.ArrayList;

public class Direction {

    public int id;
    public String start;
    public String finish;
    public ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();

    public Direction(int id, String start, String finish) {
        this.id = id;
        this.start = start;
        this.finish = finish;
    }

    public Vehicle getVehicleById(int id) {
        for (Vehicle vehicle: vehicles) {
            if (vehicle.id == id) {
                return vehicle;
            }
        }
        return null;
    }
}
