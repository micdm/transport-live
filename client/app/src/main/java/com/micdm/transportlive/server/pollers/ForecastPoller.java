package com.micdm.transportlive.server.pollers;

import android.content.Context;
import android.os.Handler;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.server.DataLoader;

public class ForecastPoller {

    public static interface OnLoadListener {
        public void onStart();
        public void onFinish();
        public void onLoad(Forecast forecast);
        public void onError();
    }

    private static final int UPDATE_INTERVAL = 30;

    private Handler handler;
    private Runnable load;
    private DataLoader loader;
    private DataLoader.Task currentTask;
    private OnLoadListener onLoadListener;

    public ForecastPoller(Context context, final OnLoadListener onLoadListener) {
        this.loader = new DataLoader(context);
        this.onLoadListener = onLoadListener;
    }

    public void start(final Service service, final SelectedStationInfo selected) {
        if (handler != null) {
            return;
        }
        handler = new Handler();
        load = new Runnable() {
            @Override
            public void run() {
                load(service, selected);
            }
        };
        load.run();
    }

    private void load(Service service, SelectedStationInfo selected) {
        handler.postDelayed(load, UPDATE_INTERVAL * 1000);
        onLoadListener.onStart();
        currentTask = loader.loadForecast(service, selected, new DataLoader.OnLoadForecastListener() {
            @Override
            public void onLoad(Forecast forecast) {
                currentTask = null;
                onLoadListener.onFinish();
                onLoadListener.onLoad(forecast);
            }
            @Override
            public void onError() {
                currentTask = null;
                onLoadListener.onFinish();
                onLoadListener.onError();
            }
        });
    }

    public void stop() {
        if (handler == null) {
            return;
        }
        handler.removeCallbacks(load);
        handler = null;
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
            onLoadListener.onFinish();
        }
    }
}
