package com.micdm.transportlive.server.pollers;

import android.content.Context;
import android.os.Handler;

import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.VehicleInfo;
import com.micdm.transportlive.server.DataLoader;

import java.util.List;

public class VehiclePoller {

    public static interface OnLoadListener {
        public void onStart();
        public void onFinish();
        public void onLoad(List<VehicleInfo> vehicles);
        public void onError();
    }

    private static final int UPDATE_INTERVAL = 30;

    private Handler handler;
    private Runnable load;
    private final DataLoader loader;
    private DataLoader.Task currentTask;
    private final OnLoadListener onLoadListener;

    public VehiclePoller(Context context, final OnLoadListener onLoadListener) {
        this.loader = new DataLoader(context);
        this.onLoadListener = onLoadListener;
    }

    public void start(final List<SelectedRouteInfo> selected) {
        if (handler != null) {
            return;
        }
        handler = new Handler();
        load = new Runnable() {
            @Override
            public void run() {
                load(selected);
            }
        };
        load.run();
    }

    private void load(List<SelectedRouteInfo> selected) {
        handler.postDelayed(load, UPDATE_INTERVAL * 1000);
        onLoadListener.onStart();
        currentTask = loader.loadVehicles(selected, new DataLoader.OnLoadVehiclesListener() {
            @Override
            public void onLoad(List<VehicleInfo> vehicles) {
                currentTask = null;
                onLoadListener.onFinish();
                onLoadListener.onLoad(vehicles);
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
