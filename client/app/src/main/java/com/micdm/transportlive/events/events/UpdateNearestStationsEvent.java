package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.SelectedStation;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

import java.util.List;

public class UpdateNearestStationsEvent extends Event {

    private final List<SelectedStation> stations;

    public UpdateNearestStationsEvent(List<SelectedStation> stations) {
        super(EventType.UPDATE_NEAREST_STATIONS);
        this.stations = stations;
    }

    public List<SelectedStation> getStations() {
        return stations;
    }
}
