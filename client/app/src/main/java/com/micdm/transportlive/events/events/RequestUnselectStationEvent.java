package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.SelectedStation;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RequestUnselectStationEvent extends Event {

    private final SelectedStation station;

    public RequestUnselectStationEvent(SelectedStation station) {
        super(EventType.REQUEST_UNSELECT_STATION);
        this.station = station;
    }

    public SelectedStation getStation() {
        return station;
    }
}
