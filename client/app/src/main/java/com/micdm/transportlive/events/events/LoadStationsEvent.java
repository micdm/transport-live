package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.SelectedStation;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

import java.util.List;

public class LoadStationsEvent extends Event {

    private final List<SelectedStation> stations;

    public LoadStationsEvent(List<SelectedStation> stations) {
        super(EventType.LOAD_STATIONS);
        this.stations = stations;
    }

    public List<SelectedStation> getStations() {
        return stations;
    }
}
