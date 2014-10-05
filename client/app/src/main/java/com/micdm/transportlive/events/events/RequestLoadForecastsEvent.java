package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RequestLoadForecastsEvent extends Event {

    public RequestLoadForecastsEvent() {
        super(EventType.REQUEST_LOAD_FORECASTS);
    }
}
