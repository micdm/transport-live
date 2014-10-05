package com.micdm.transportlive.data;

import java.util.List;

public class Transport {

    public static enum Type {
        TROLLEYBUS,
        TRAM
    }

    private final int id;
    private final List<Station> stations;
    private final List<Route> routes;

    public Transport(int id, List<Station> stations, List<Route> routes) {
        this.id = id;
        this.stations = stations;
        this.routes = routes;
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return Type.values()[id];
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

    public List<Route> getRoutes() {
        return routes;
    }

    public Route getRouteByNumber(int number) {
        for (Route route: routes) {
            if (route.getNumber() == number) {
                return route;
            }
        }
        return null;
    }
}
