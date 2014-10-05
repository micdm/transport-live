package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RequestLoadRoutesEvent extends Event {

    public RequestLoadRoutesEvent() {
        super(EventType.REQUEST_LOAD_ROUTES);
    }
}
