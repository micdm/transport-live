package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RemoveAllVehiclesEvent extends Event {

    public RemoveAllVehiclesEvent() {
        super(EventType.REMOVE_ALL_VEHICLES);
    }
}
