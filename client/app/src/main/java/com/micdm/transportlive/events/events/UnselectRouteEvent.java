package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.SelectedRoute;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class UnselectRouteEvent extends Event {

    private final SelectedRoute route;

    public UnselectRouteEvent(SelectedRoute route) {
        super(EventType.UNSELECT_ROUTE);
        this.route = route;
    }

    public SelectedRoute getRoute() {
        return route;
    }
}
