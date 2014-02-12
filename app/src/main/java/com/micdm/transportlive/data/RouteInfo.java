package com.micdm.transportlive.data;

public class RouteInfo {

    public Transport.Type transport;
    public Route route;
    public String start;
    public String finish;

    public RouteInfo(Transport.Type transport, Route route, String start, String finish) {
        this.transport = transport;
        this.route = route;
        this.start = start;
        this.finish = finish;
    }
}
