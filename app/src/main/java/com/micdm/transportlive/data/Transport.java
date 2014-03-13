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
    public ArrayList<Route> routes = new ArrayList<Route>();

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

    public List<RouteInfo> getAllRouteInfo() {
        ArrayList<RouteInfo> info = new ArrayList<RouteInfo>();
        for (Route route: routes) {
            Direction direction = route.directions.get(0);
            info.add(new RouteInfo(this, route, direction.getStart().name, direction.getFinish().name));
        }
        return info;
    }
}
