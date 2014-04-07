package com.micdm.transportlive.interfaces;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.SelectedStationInfo;

public interface ForecastHandler {

    public static interface OnSelectStationListener extends EventListener {
        public void onSelectStation(SelectedStationInfo selected);
    }

    public static interface OnLoadForecastListener extends EventListener {
        public void onStart();
        public void onFinish();
        public void onLoadForecast(Forecast forecast);
    }

    public void requestStationSelection();
    public void selectStation(SelectedStationInfo selected);
    public void loadForecast();
    public void addOnSelectStationListener(OnSelectStationListener listener);
    public void removeOnSelectStationListener(OnSelectStationListener listener);
    public void addOnLoadForecastListener(OnLoadForecastListener listener);
    public void removeOnLoadForecastListener(OnLoadForecastListener listener);
}
