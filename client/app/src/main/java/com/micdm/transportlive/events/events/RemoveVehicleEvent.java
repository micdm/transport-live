package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RemoveVehicleEvent extends Event {

    private final String number;

    public RemoveVehicleEvent(String number) {
        super(EventType.REMOVE_VEHICLE);
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
}
