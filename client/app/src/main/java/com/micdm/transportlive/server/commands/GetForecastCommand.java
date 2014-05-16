package com.micdm.transportlive.server.commands;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Service;

public class GetForecastCommand implements Command {

    public static class Result implements Command.Result {

        public final Forecast forecast;

        public Result(Forecast forecast) {
            this.forecast = forecast;
        }
    }

    public final Service service;
    public final SelectedStationInfo selected;

    public GetForecastCommand(Service service, SelectedStationInfo selected) {
        this.service = service;
        this.selected = selected;
    }
}
