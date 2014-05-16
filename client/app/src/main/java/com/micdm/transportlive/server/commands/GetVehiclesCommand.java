package com.micdm.transportlive.server.commands;

import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.VehicleInfo;

import java.util.List;

public class GetVehiclesCommand implements Command {

    public static class Result implements Command.Result {

        public final List<VehicleInfo> vehicles;

        public Result(List<VehicleInfo> vehicles) {
            this.vehicles = vehicles;
        }
    }

    public final List<SelectedRouteInfo> selected;

    public GetVehiclesCommand(List<SelectedRouteInfo> selected) {
        this.selected = selected;
    }
}
