package com.micdm.transportlive.data.service;

import java.util.List;

public class Direction {

    private final int id;
    private final List<Station> stations;
    private final List<Point> points;

    public Direction(int id, List<Station> stations, List<Point> points) {
        this.id = id;
        this.stations = stations;
        this.points = points;
    }

    public int getId() {
        return id;
    }

    public List<Station> getStations() {
        return stations;
    }

    public Station getStationById(int id) {
        for (Station station: stations) {
            if (station.getId() == id) {
                return station;
            }
        }
        return null;
    }

    public String getStart() {
        return stations.get(0).getName();
    }

    public String getFinish() {
        return stations.get(stations.size() - 1).getName();
    }

    public List<Point> getPoints() {
        return points;
    }
}
