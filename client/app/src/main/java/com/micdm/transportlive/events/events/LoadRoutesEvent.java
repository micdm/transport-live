package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.SelectedRoute;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

import java.util.List;

public class LoadRoutesEvent extends Event {

    private final List<SelectedRoute> routes;

    public LoadRoutesEvent(List<SelectedRoute> routes) {
        super(EventType.LOAD_ROUTES);
        this.routes = routes;
    }

    public List<SelectedRoute> getRoutes() {
        return routes;
    }
}
