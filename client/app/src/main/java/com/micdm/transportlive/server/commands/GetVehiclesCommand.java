package com.micdm.transportlive.server.commands;

import com.micdm.transportlive.data.RouteInfo;
import com.micdm.transportlive.data.SelectedRouteInfo;

import java.util.List;

public class GetVehiclesCommand implements Command {

    public static class Result implements Command.Result {

        public final List<RouteInfo> routes;

        public Result(List<RouteInfo> routes) {
            this.routes = routes;
        }
    }

    public final List<SelectedRouteInfo> selected;

    public GetVehiclesCommand(List<SelectedRouteInfo> selected) {
        this.selected = selected;
    }
}
