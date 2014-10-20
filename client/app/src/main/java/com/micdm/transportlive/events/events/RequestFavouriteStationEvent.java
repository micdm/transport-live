package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.SelectedStation;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RequestFavouriteStationEvent extends Event {

    private final SelectedStation station;

    public RequestFavouriteStationEvent(SelectedStation station) {
        super(EventType.REQUEST_FAVOURITE_STATION);
        this.station = station;
    }

    public SelectedStation getStation() {
        return station;
    }
}
