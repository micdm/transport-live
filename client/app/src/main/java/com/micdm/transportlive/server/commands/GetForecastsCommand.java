package com.micdm.transportlive.server.commands;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Service;

import java.util.List;

public class GetForecastsCommand implements Command {

    public static class Result implements Command.Result {

        public final List<Forecast> forecasts;

        public Result(List<Forecast> forecasts) {
            this.forecasts = forecasts;
        }
    }

    public final Service service;
    public final List<SelectedStationInfo> selected;

    public GetForecastsCommand(Service service, List<SelectedStationInfo> selected) {
        this.service = service;
        this.selected = selected;
    }
}
