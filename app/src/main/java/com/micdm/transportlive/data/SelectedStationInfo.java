package com.micdm.transportlive.data;

public class SelectedStationInfo {

    public Transport transport;
    public Route route;
    public Direction direction;
    public Station station;

    public SelectedStationInfo(Transport transport, Route route, Direction direction, Station station) {
        this.transport = transport;
        this.route = route;
        this.direction = direction;
        this.station = station;
    }
}
