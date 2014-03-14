package com.micdm.transportlive.data;

import java.util.ArrayList;
import java.util.List;

public class Direction {

    public int id;
    public List<Station> stations = new ArrayList<Station>();
    public List<Vehicle> vehicles = new ArrayList<Vehicle>();

    public Direction(int id) {
        this.id = id;
    }

    public Station getStart() {
        return stations.get(0);
    }

    public Station getFinish() {
        return stations.get(stations.size() - 1);
    }

    public Vehicle getVehicleById(String id) {
        for (Vehicle vehicle: vehicles) {
            if (vehicle.id.equals(id)) {
                return vehicle;
            }
        }
        return null;
    }
}
