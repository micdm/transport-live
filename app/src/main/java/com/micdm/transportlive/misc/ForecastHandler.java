package com.micdm.transportlive.misc;

import com.micdm.transportlive.data.SelectedStationInfo;

public interface ForecastHandler {

    public static interface OnSelectStationListener {
        public void onSelectStation(SelectedStationInfo selected);
    }

    public static interface OnLoadForecastListener {
        public void onLoadForecast();
    }

    public void requestStationSelection();
    public void selectStation(SelectedStationInfo selected);
    public void setOnSelectStationListener(OnSelectStationListener listener);
    public void setOnLoadForecastListener(OnLoadForecastListener listener);
}
