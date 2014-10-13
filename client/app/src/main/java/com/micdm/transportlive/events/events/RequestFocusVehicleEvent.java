package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RequestFocusVehicleEvent extends Event {

    private final String number;
    private final int transportId;
    private final int routeNumber;

    public RequestFocusVehicleEvent(String number, int transportId, int routeNumber) {
        super(EventType.REQUEST_FOCUS_VEHICLE);
        this.transportId = transportId;
        this.routeNumber = routeNumber;
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public int getTransportId() {
        return transportId;
    }

    public int getRouteNumber() {
        return routeNumber;
    }
}
