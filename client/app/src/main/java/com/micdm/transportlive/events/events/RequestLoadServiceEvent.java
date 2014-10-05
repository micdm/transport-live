package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RequestLoadServiceEvent extends Event {

    public RequestLoadServiceEvent() {
        super(EventType.REQUEST_LOAD_SERVICE);
    }
}
