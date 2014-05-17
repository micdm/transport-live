package com.micdm.transportlive.server.commands;

import com.micdm.transportlive.data.RouteInfo;
import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.Service;

import java.util.List;

public class GetVehiclesCommand implements Command {

    public static class Result implements Command.Result {

        public final List<RouteInfo> routes;

        public Result(List<RouteInfo> routes) {
            this.routes = routes;
        }
    }

    public final Service service;
    public final List<SelectedRouteInfo> selected;

    public GetVehiclesCommand(Service service, List<SelectedRouteInfo> selected) {
        this.service = service;
        this.selected = selected;
    }
}
