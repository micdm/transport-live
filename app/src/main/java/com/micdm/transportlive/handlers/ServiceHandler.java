package com.micdm.transportlive.handlers;

import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.data.VehicleInfo;

import java.util.List;

public interface ServiceHandler {

    public static interface OnUnselectAllRoutesListener {
        public void onUnselectAllRoutes();
    }

    public static interface OnLoadServiceListener {
        public void onLoadService(Service service);
    }

    public static interface OnLoadVehiclesListener {
        public void onStart();
        public void onFinish();
        public void onLoadVehicles(List<VehicleInfo> vehicles);
        public void onError();
    }

    public Service getService();
    public boolean isRouteSelected(Transport transport, Route route);
    public void selectRoute(Transport transport, Route route, boolean isSelected);
    public void loadVehicles();
    public void setOnUnselectAllRoutesListener(OnUnselectAllRoutesListener listener);
    public void setOnLoadServiceListener(OnLoadServiceListener listener);
    public void setOnLoadVehiclesListener(OnLoadVehiclesListener listener);
}
