package com.micdm.transportlive.misc;

import android.content.Context;
import android.os.Handler;

import com.micdm.transportlive.data.Service;

public class VehiclePoller {

    public static interface OnLoadListener {
        public void onLoad(Service service);
        public void onNoConnection();
    }

    private static final int UPDATE_INTERVAL = 30;

    private Handler handler;
    private Runnable load;
    private VehicleLoader loader;
    private VehicleLoader.Task currentTask;
    private OnLoadListener onLoadListener;

    public VehiclePoller(Context context, OnLoadListener onLoadListener) {
        this.loader = new VehicleLoader(context, new VehicleLoader.OnNoConnectionListener() {
            @Override
            public void onNoConnection() {

            }
        });
        this.onLoadListener = onLoadListener;
    }

    private void load(Service service) {
        currentTask = loader.load(service, new VehicleLoader.OnLoadListener() {
            @Override
            public void onLoad(Service service) {
                currentTask = null;
                onLoadListener.onLoad(service);
            }
        });
        handler.postDelayed(load, UPDATE_INTERVAL * 1000);
    }

    public void start(final Service service) {
        if (handler != null) {
            return;
        }
        handler = new Handler();
        load = new Runnable() {
            @Override
            public void run() {
                load(service);
            }
        };
        load.run();
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
        }
    }
}
