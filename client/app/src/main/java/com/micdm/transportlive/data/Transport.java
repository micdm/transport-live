package com.micdm.transportlive.data;

import java.util.ArrayList;
import java.util.List;

public class Transport {

    public static enum Type {
        TROLLEYBUS,
        TRAM
    }

    public final int id;
    public final List<Station> stations = new ArrayList<Station>();
    public final List<Route> routes = new ArrayList<Route>();

    public Transport(int id) {
        this.id = id;
    }

    public Type getType() {
        return Type.values()[id];
    }

    public Station getStationById(int id) {
        for (Station station: stations) {
            if (station.id == id) {
                return station;
            }
        }
        return null;
    }

    public Route getRouteByNumber(int number) {
        for (Route route: routes) {
            if (route.number == number) {
                return route;
            }
        }
        return null;
    }
}
