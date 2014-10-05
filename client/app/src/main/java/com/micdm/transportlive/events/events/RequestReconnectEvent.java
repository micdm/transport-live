package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RequestReconnectEvent extends Event {

    public RequestReconnectEvent() {
        super(EventType.REQUEST_RECONNECT);
    }
}
