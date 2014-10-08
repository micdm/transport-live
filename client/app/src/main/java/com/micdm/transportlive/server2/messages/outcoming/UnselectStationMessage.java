package com.micdm.transportlive.server2.messages.outcoming;

import com.micdm.transportlive.server2.messages.Message;

public class UnselectStationMessage implements Message {

    private final int transportId;
    private final int stationId;

    public UnselectStationMessage(int transportId, int stationId) {
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
