package com.micdm.transportlive.server.commands;

import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.Service;

import java.util.List;

public class GetVehiclesCommand implements Command {

    public static class Result implements Command.Result {

        public Service service;

        public Result(Service service) {
            this.service = service;
        }
    }

    public Service service;
    public List<SelectedRouteInfo> selected;

    public GetVehiclesCommand(Service service, List<SelectedRouteInfo> selected) {
        this.service = service;
        this.selected = selected;
    }
}
