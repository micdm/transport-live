package com.micdm.transportlive.server.pollers;

import android.content.Context;

import com.micdm.transportlive.data.RouteInfo;
import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.server.DataLoader;

import java.util.List;

public class VehiclePoller extends Poller<List<SelectedRouteInfo>, List<RouteInfo>> {

    public static interface OnLoadListener extends Poller.OnLoadListener<List<RouteInfo>> {}

    public VehiclePoller(Context context, Poller.OnLoadListener listener) {
        super(context, listener);
    }

    @Override
    protected DataLoader.Task startTask(Service service, List<SelectedRouteInfo> selected) {
        return loader.loadVehicles(service, selected, new DataLoader.OnLoadVehiclesListener() {
            @Override
            public void onLoad(List<RouteInfo> vehicles) {
                onTaskResult(vehicles);
            }
            @Override
            public void onError() {
                onTaskError();
            }
        });
    }
}
