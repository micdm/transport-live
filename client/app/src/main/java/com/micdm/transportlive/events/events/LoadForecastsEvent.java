package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

import java.util.List;

public class LoadForecastsEvent extends Event {

    public static final int STATE_START = 0;
    public static final int STATE_FINISH = 1;
    public static final int STATE_COMPLETE = 2;

    private final int state;
    private final List<Forecast> forecasts;

    public LoadForecastsEvent(int state) {
        this(state, null);
    }

    public LoadForecastsEvent(int state, List<Forecast> forecasts) {
        super(EventType.LOAD_FORECASTS);
        this.state = state;
        this.forecasts = forecasts;
    }

    public int getState() {
        return state;
    }

    public List<Forecast> getForecasts() {
        return forecasts;
    }
}
