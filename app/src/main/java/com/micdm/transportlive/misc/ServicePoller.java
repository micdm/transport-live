package com.micdm.transportlive.misc;

import android.os.Handler;

import com.micdm.transportlive.data.Service;

public class ServicePoller {

    public static interface OnLoadListener {
        public void onLoad(Service service);
    }

    private static final int UPDATE_INTERVAL = 30;

    private Handler handler;
    private Runnable load;
    private ServiceLoader loader;
    private ServiceLoader.Task currentTask;
    private OnLoadListener onLoadListener;

    public ServicePoller(ServiceLoader loader, OnLoadListener onLoadListener) {
        this.loader = loader;
        this.onLoadListener = onLoadListener;
    }

    private void load(Service service) {
        currentTask = loader.loadVehicles(service, new ServiceLoader.OnLoadListener() {
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

    public void restart(Service service) {
        stop();
        start(service);
    }
}
