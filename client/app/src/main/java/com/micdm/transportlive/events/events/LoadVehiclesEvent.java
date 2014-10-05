package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.RoutePopulation;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

import java.util.List;

public class LoadVehiclesEvent extends Event {

    public static final int STATE_START = 0;
    public static final int STATE_FINISH = 1;
    public static final int STATE_COMPLETE = 2;

    private final int state;
    private final List<RoutePopulation> populations;

    public LoadVehiclesEvent(int state) {
        this(state, null);
    }

    public LoadVehiclesEvent(int state, List<RoutePopulation> populations) {
        super(EventType.LOAD_VEHICLES);
        this.state = state;
        this.populations = populations;
    }

    public int getState() {
        return state;
    }

    public List<RoutePopulation> getPopulations() {
        return populations;
    }
}
