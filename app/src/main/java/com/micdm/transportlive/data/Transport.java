package com.micdm.transportlive.data;

import java.util.ArrayList;

public class Transport {

    public static enum Type {
        BUS,
        TROLLEYBUS,
        TRAM,
        TAXI
    }

    public int id;
    public Type type;
    public ArrayList<Route> routes = new ArrayList<Route>();

    public Transport(int id, Type type) {
        this.id = id;
        this.type = type;
    }

    public RouteInfo[] getAllRouteInfo() {
        ArrayList<RouteInfo> info = new ArrayList<RouteInfo>();
        for (Route route: routes) {
            Direction direction = route.directions.get(0);
            info.add(new RouteInfo(type, route, direction.start, direction.finish));
        }
        return info.toArray(new RouteInfo[info.size()]);
    }
}
