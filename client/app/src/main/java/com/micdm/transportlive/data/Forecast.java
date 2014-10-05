package com.micdm.transportlive.data;

import java.util.List;

public class Forecast {

    private final int transportId;
    private final int stationId;
    private final List<ForecastVehicle> vehicles;

    public Forecast(int transportId, int stationId, List<ForecastVehicle> vehicles) {
        this.transportId = transportId;
        this.stationId = stationId;
        this.vehicles = vehicles;
    }

    public int getTransportId() {
        return transportId;
    }

    public int getStationId() {
        return stationId;
    }

    public List<ForecastVehicle> getVehicles() {
        return vehicles;
    }
}
