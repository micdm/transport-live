package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.Vehicle;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class UpdateVehicleEvent extends Event {

    private final Vehicle vehicle;

    public UpdateVehicleEvent(Vehicle vehicle) {
        super(EventType.UPDATE_VEHICLE);
        this.vehicle = vehicle;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }
}
