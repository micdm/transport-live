package com.micdm.transportlive.data;

import java.util.ArrayList;
import java.util.List;

public class Forecast {

    public final Transport transport;
    public final Station station;
    public final List<ForecastVehicle> vehicles = new ArrayList<ForecastVehicle>();

    public Forecast(Transport transport, Station station) {
        this.transport = transport;
        this.station = station;
    }
}
