package com.micdm.transportlive.misc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.Service;

import java.util.List;

public class VehiclePoller {

    public static interface OnLoadListener {
        public void onLoad(Service service);
        public void onNoConnection();
    }

    private static final int UPDATE_INTERVAL = 30;

    private Handler handler;
    private Runnable load;
    private Context context;
    private VehicleLoader loader;
    private VehicleLoader.Task currentTask;
    private OnLoadListener onLoadListener;

    public VehiclePoller(Context context, final OnLoadListener onLoadListener) {
        this.context = context;
        this.loader = new VehicleLoader(context);
        this.onLoadListener = onLoadListener;
    }

    public void start(final Service service, final List<SelectedRouteInfo> selected) {
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

    private void load(Service service, List<SelectedRouteInfo> selected) {
        handler.postDelayed(load, UPDATE_INTERVAL * 1000);
        if (isNetworkAvailable()) {
            currentTask = loader.load(service, selected, new VehicleLoader.OnLoadListener() {
                @Override
                public void onLoad(Service service) {
                    currentTask = null;
                    onLoadListener.onLoad(service);
                }
            });
        } else {
            onLoadListener.onNoConnection();
        }
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

    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
