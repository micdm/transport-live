package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.ForecastVehicle;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class UpdateForecastEvent extends Event {

    private final ForecastVehicle vehicle;

    public UpdateForecastEvent(ForecastVehicle vehicle) {
        super(EventType.UPDATE_FORECAST);
        this.vehicle = vehicle;
    }

    public ForecastVehicle getVehicle() {
        return vehicle;
    }
}
