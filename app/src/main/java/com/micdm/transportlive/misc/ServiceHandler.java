package com.micdm.transportlive.misc;

import com.micdm.transportlive.data.RouteInfo;
import com.micdm.transportlive.data.Service;

public interface ServiceHandler {

    public Service getService();
    public void onSelectRoute(RouteInfo info, boolean isSelected);
}
