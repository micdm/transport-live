package com.micdm.transportlive.interfaces;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Station;
import com.micdm.transportlive.data.Transport;

import java.util.List;

public interface ForecastHandler {

    public static interface OnLoadStationsListener extends EventListener {
        public void onLoadStations(List<SelectedStationInfo> selected);
    }

    public static interface OnSelectStationListener extends EventListener {
        public void onSelectStation(SelectedStationInfo selected);
    }

    public static interface OnUnselectStationListener extends EventListener {
        public void onUnselectStation(Transport transport, Station station);
    }

    public static interface OnUnselectAllStationsListener extends EventListener {
        public void onUnselectAllStations();
    }

    public static interface OnLoadForecastsListener extends EventListener {
        public void onStart();
        public void onFinish();
        public void onLoadForecasts(List<Forecast> forecasts);
    }

    public void requestStationSelection();
    public void selectStation(SelectedStationInfo selected);
    public void unselectStation(Transport transport, Station station);
    public void addOnLoadStationsListener(OnLoadStationsListener listener);
    public void removeOnLoadStationsListener(OnLoadStationsListener listener);
    public void addOnSelectStationListener(OnSelectStationListener listener);
    public void removeOnSelectStationListener(OnSelectStationListener listener);
    public void addOnUnselectStationListener(OnUnselectStationListener listener);
    public void removeOnUnselectStationListener(OnUnselectStationListener listener);
    public void addOnUnselectAllStationsListener(OnUnselectAllStationsListener listener);
    public void removeOnUnselectAllStationsListener(OnUnselectAllStationsListener listener);
    public void addOnLoadForecastsListener(OnLoadForecastsListener listener);
    public void removeOnLoadForecastsListener(OnLoadForecastsListener listener);
}
