package com.micdm.transportlive.misc;

import com.micdm.transportlive.data.RouteInfo;
import com.micdm.transportlive.data.Service;

public interface ServiceHandler {

    public static interface OnLoadServiceListener {
        public void onLoadService(Service service);
    }

    public static interface OnLoadVehiclesListener {
        public void onLoadVehicles(Service service);
    }

    public Service getService();
    public void selectRoute(RouteInfo info, boolean isSelected);
    public void setOnLoadServiceListener(OnLoadServiceListener listener);
    public void setOnLoadVehiclesListener(OnLoadVehiclesListener listener);
}
