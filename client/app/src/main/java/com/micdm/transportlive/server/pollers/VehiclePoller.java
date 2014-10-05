package com.micdm.transportlive.server.pollers;

import android.content.Context;

import com.micdm.transportlive.data.RoutePopulation;
import com.micdm.transportlive.data.SelectedRoute;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.server.DataLoader;

import java.util.List;

public class VehiclePoller extends Poller<List<SelectedRoute>, List<RoutePopulation>> {

    public static interface OnLoadListener extends Poller.OnLoadListener<List<RoutePopulation>> {}

    public VehiclePoller(Context context, Poller.OnLoadListener listener) {
        super(context, listener);
    }

    @Override
    protected DataLoader.Task startTask(Service service, List<SelectedRoute> selected) {
        return loader.loadVehicles(service, selected, new DataLoader.OnLoadVehiclesListener() {
            @Override
            public void onLoad(List<RoutePopulation> vehicles) {
                onTaskResult(vehicles);
            }
            @Override
            public void onError() {
                onTaskError();
            }
        });
    }
}
