package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.SelectedRoute;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RequestUnselectRouteEvent extends Event {

    private final SelectedRoute route;

    public RequestUnselectRouteEvent(SelectedRoute route) {
        super(EventType.REQUEST_UNSELECT_ROUTE);
        this.route = route;
    }

    public SelectedRoute getRoute() {
        return route;
    }
}
