package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.SelectedStation;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RequestSelectStationEvent extends Event {

    private final SelectedStation station;

    public RequestSelectStationEvent(SelectedStation station) {
        super(EventType.REQUEST_SELECT_STATION);
        this.station = station;
    }

    public SelectedStation getStation() {
        return station;
    }
}
