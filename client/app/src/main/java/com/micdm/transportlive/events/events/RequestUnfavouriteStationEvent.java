package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.SelectedStation;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RequestUnfavouriteStationEvent extends Event {

    private final SelectedStation station;

    public RequestUnfavouriteStationEvent(SelectedStation station) {
        super(EventType.REQUEST_UNFAVOURITE_STATION);
        this.station = station;
    }

    public SelectedStation getStation() {
        return station;
    }
}
