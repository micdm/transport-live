package com.micdm.transportlive.server.messages.outcoming;

import com.micdm.transportlive.server.messages.Message;

public class SelectStationMessage implements Message {

    private final int transportId;
    private final int stationId;

    public SelectStationMessage(int transportId, int stationId) {
        this.transportId = transportId;
        this.stationId = stationId;
    }

    public int getTransportId() {
        return transportId;
    }

    public int getStationId() {
        return stationId;
    }
}
