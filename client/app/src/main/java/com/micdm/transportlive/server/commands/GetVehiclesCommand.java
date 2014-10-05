package com.micdm.transportlive.server.commands;

import com.micdm.transportlive.data.RoutePopulation;
import com.micdm.transportlive.data.SelectedRoute;
import com.micdm.transportlive.data.Service;

import java.util.List;

public class GetVehiclesCommand implements Command {

    public static class Result implements Command.Result {

        public final List<RoutePopulation> routes;

        public Result(List<RoutePopulation> routes) {
            this.routes = routes;
        }
    }

    public final Service service;
    public final List<SelectedRoute> selected;

    public GetVehiclesCommand(Service service, List<SelectedRoute> selected) {
        this.service = service;
        this.selected = selected;
    }
}
