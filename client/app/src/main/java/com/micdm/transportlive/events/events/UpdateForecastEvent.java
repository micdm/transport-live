package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class UpdateForecastEvent extends Event {

    private final Forecast forecast;

    public UpdateForecastEvent(Forecast forecast) {
        super(EventType.UPDATE_FORECAST);
        this.forecast = forecast;
    }

    public Forecast getForecast() {
        return forecast;
    }
}
