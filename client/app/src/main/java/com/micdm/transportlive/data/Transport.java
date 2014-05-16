package com.micdm.transportlive.data;

import java.util.ArrayList;
import java.util.List;

public class Transport {

    public static enum Type {
        TROLLEYBUS,
        TRAM
    }

    public int id;
    public List<Route> routes = new ArrayList<Route>();

    public Transport(int id) {
        this.id = id;
    }

    public Type getType() {
        return Type.values()[id];
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
