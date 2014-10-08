package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.MapVehicle;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class UpdateVehicleEvent extends Event {

    private final MapVehicle vehicle;

    public UpdateVehicleEvent(MapVehicle vehicle) {
        super(EventType.UPDATE_VEHICLE);
        this.vehicle = vehicle;
    }

    public MapVehicle getVehicle() {
        return vehicle;
    }
}
