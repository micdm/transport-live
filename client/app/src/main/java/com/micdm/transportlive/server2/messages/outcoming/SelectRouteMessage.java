package com.micdm.transportlive.server2.messages.outcoming;

import com.micdm.transportlive.server2.messages.Message;

public class SelectRouteMessage implements Message {

    private final int transportId;
    private final int routeNumber;

    public SelectRouteMessage(int transportId, int routeNumber) {
        this.transportId = transportId;
        this.routeNumber = routeNumber;
    }

    public int getTransportId() {
        return transportId;
    }

    public int getRouteNumber() {
        return routeNumber;
    }
}
