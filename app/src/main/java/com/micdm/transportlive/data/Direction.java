package com.micdm.transportlive.data;

import java.util.ArrayList;
import java.util.List;

public class Direction {

    public int id;
    public List<Station> stations = new ArrayList<Station>();

    public Direction(int id) {
        this.id = id;
    }

    public Station getStart() {
        return stations.get(0);
    }

    public Station getFinish() {
        return stations.get(stations.size() - 1);
    }
}
