package com.micdm.transportlive.data;

import java.util.ArrayList;
import java.util.List;

public class RouteInfo {

    public final Transport transport;
    public final Route route;
    public final List<Vehicle> vehicles = new ArrayList<Vehicle>();

    public RouteInfo(Transport transport, Route route) {
        this.transport = transport;
        this.route = route;
    }
}
