package com.micdm.transportlive.server.pollers;

import android.content.Context;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.server.DataLoader;

import java.util.List;

public class ForecastPoller extends Poller<List<SelectedStationInfo>, List<Forecast>> {

    public static interface OnLoadListener extends Poller.OnLoadListener<List<Forecast>> {}

    public ForecastPoller(Context context, Poller.OnLoadListener listener) {
        super(context, listener);
    }

    @Override
    protected DataLoader.Task startTask(Service service, List<SelectedStationInfo> selected) {
        return loader.loadForecast(service, selected, new DataLoader.OnLoadForecastListener() {
            @Override
            public void onLoad(List<Forecast> forecasts) {
                onTaskResult(forecasts);
            }
            @Override
            public void onError() {
                onTaskError();
            }
        });
    }
}
