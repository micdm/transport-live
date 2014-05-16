package com.micdm.transportlive.data;

import java.util.ArrayList;
import java.util.List;

public class Direction {

    public final int id;
    public final List<Station> stations = new ArrayList<Station>();

    public Direction(int id) {
        this.id = id;
    }

    public String getStart() {
        return stations.get(0).name;
    }

    public String getFinish() {
        return stations.get(stations.size() - 1).name;
    }

    public Station getStationById(int id) {
        for (Station station: stations) {
            if (station.id == id) {
                return station;
            }
        }
        return null;
    }
}
