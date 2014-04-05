package com.micdm.transportlive.handlers;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.SelectedStationInfo;

public interface ForecastHandler {

    public static interface OnSelectStationListener {
        public void onSelectStation(SelectedStationInfo selected);
    }

    public static interface OnLoadForecastListener {
        public void onStart();
        public void onFinish();
        public void onLoadForecast(Forecast forecast);
        public void onError();
    }

    public void requestStationSelection();
    public void selectStation(SelectedStationInfo selected);
    public void loadForecast();
    public void setOnSelectStationListener(OnSelectStationListener listener);
    public void setOnLoadForecastListener(OnLoadForecastListener listener);
}
