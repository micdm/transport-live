package com.micdm.transportlive.server.pollers;

import android.content.Context;
import android.os.Handler;

import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.server.DataLoader;

public abstract class Poller<Selected, Result> {

    public static interface OnLoadListener<Result> {
        public void onStart();
        public void onFinish();
        public void onLoad(Result result);
        public void onError();
    }

    private static final int UPDATE_INTERVAL = 15;

    private Handler handler = new Handler();
    private Runnable load;
    protected final DataLoader loader;
    private DataLoader.Task currentTask;
    private final OnLoadListener onLoadListener;

    public Poller(Context context, OnLoadListener listener) {
        this.loader = new DataLoader(context);
        this.onLoadListener = listener;
    }

    public void start(final Service service, final Selected selected) {
        if (load != null) {
            return;
        }
        load = new Runnable() {
            @Override
            public void run() {
                startLoading(service, selected);
            }
        };
        load.run();
    }

    public void stop() {
        if (load == null) {
            return;
        }
        handler.removeCallbacks(load);
        load = null;
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
            onLoadListener.onFinish();
        }
    }

    private void startLoading(Service service, Selected selected) {
        onLoadListener.onStart();
        currentTask = startTask(service, selected);
        handler.postDelayed(load, UPDATE_INTERVAL * 1000);
    }

    protected abstract DataLoader.Task startTask(Service service, Selected selected);

    protected void onTaskResult(Result result) {
        currentTask = null;
        onLoadListener.onFinish();
        onLoadListener.onLoad(result);
    }

    protected void onTaskError() {
        currentTask = null;
        onLoadListener.onFinish();
        onLoadListener.onError();
    }
}
