package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RequestLoadVehiclesEvent extends Event {

    public RequestLoadVehiclesEvent() {
        super(EventType.REQUEST_LOAD_VEHICLES);
    }
}
