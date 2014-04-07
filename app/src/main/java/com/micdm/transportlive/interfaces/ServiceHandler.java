package com.micdm.transportlive.interfaces;

import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.data.VehicleInfo;

import java.util.List;

public interface ServiceHandler {

    public static interface OnUnselectAllRoutesListener extends EventListener {
        public void onUnselectAllRoutes();
    }

    public static interface OnLoadServiceListener extends EventListener {
        public void onLoadService(Service service);
    }

    public static interface OnLoadVehiclesListener extends EventListener {
        public void onStart();
        public void onFinish();
        public void onLoadVehicles(List<VehicleInfo> vehicles);
    }

    public void requestRouteSelection();
    public boolean isRouteSelected(Transport transport, Route route);
    public void selectRoute(Transport transport, Route route, boolean isSelected);
    public void loadVehicles();
    public void addOnUnselectAllRoutesListener(OnUnselectAllRoutesListener listener);
    public void removeOnUnselectAllRoutesListener(OnUnselectAllRoutesListener listener);
    public void addOnLoadServiceListener(OnLoadServiceListener listener);
    public void removeOnLoadServiceListener(OnLoadServiceListener listener);
    public void addOnLoadVehiclesListener(OnLoadVehiclesListener listener);
    public void removeOnLoadVehiclesListener(OnLoadVehiclesListener listener);
}
