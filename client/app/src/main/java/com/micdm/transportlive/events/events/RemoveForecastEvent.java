package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RemoveForecastEvent extends Event {

    private final int transportId;
    private final int stationId;
    private final String number;

    public RemoveForecastEvent(int transportId, int stationId, String number) {
        super(EventType.REMOVE_FORECAST);
        this.transportId = transportId;
        this.stationId = stationId;
        this.number = number;
    }

    public int getTransportId() {
        return transportId;
    }

    public int getStationId() {
        return stationId;
    }

    public String getNumber() {
        return number;
    }
}
