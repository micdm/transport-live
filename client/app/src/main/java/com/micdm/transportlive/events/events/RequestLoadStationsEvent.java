package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RequestLoadStationsEvent extends Event {

    public RequestLoadStationsEvent() {
        super(EventType.REQUEST_LOAD_STATIONS);
    }
}
