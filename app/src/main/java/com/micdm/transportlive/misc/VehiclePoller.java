package com.micdm.transportlive.misc;

import android.content.Context;
import android.os.Handler;

import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.VehicleInfo;

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
    private VehicleLoader loader;
    private VehicleLoader.Task currentTask;
    private OnLoadListener onLoadListener;

    public VehiclePoller(Context context, final OnLoadListener onLoadListener) {
        this.loader = new VehicleLoader(context);
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
        currentTask = loader.load(selected, new VehicleLoader.OnLoadListener() {
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
