package com.micdm.transportlive.data;

import java.util.ArrayList;
import java.util.List;

public class Transport {

    public static enum Type {
        BUS,
        TROLLEYBUS,
        TRAM,
        TAXI
    }

    public int id;
    public Type type;
    public String code;
    public List<Route> routes = new ArrayList<Route>();

    public Transport(int id, Type type, String code) {
        this.id = id;
        this.type = type;
        this.code = code;
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
