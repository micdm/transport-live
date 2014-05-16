package com.micdm.transportlive.data;

public class SelectedStationInfo {

    public final Transport transport;
    public final Route route;
    public final Direction direction;
    public final Station station;

    public SelectedStationInfo(Transport transport, Route route, Direction direction, Station station) {
        this.transport = transport;
        this.route = route;
        this.direction = direction;
        this.station = station;
    }
}
