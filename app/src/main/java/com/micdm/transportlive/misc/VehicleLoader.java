package com.micdm.transportlive.misc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.server.ServerConnectTask;
import com.micdm.transportlive.server.commands.Command;
import com.micdm.transportlive.server.commands.GetVehiclesCommand;

import java.util.List;

public class VehicleLoader {

    public static interface OnNoConnectionListener {
        public void onNoConnection();
    }

    public static interface OnLoadListener {
        public void onLoad(Service service);
    }

    public static class Task {

        private ServerConnectTask task;

        public Task(ServerConnectTask task) {
            this.task = task;
        }

        public void cancel() {
            task.cancel(true);
        }
    }

    private Context context;
    private OnNoConnectionListener noConnectionListener;

    public VehicleLoader(Context context, OnNoConnectionListener noConnectionListener) {
        this.context = context;
        this.noConnectionListener = noConnectionListener;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public Task load(Service service, List<SelectedRouteInfo> selected, final OnLoadListener listener) {
        if (!isNetworkAvailable()) {
            noConnectionListener.onNoConnection();
            return null;
        }
        ServerConnectTask task = new ServerConnectTask(context, new ServerConnectTask.OnResultListener() {
            @Override
            public void onResult(Command.Result result) {
                Service service = ((GetVehiclesCommand.Result)result).service;
                listener.onLoad(service);
            }
        });
        task.execute(new GetVehiclesCommand(service, selected));
        return new Task(task);
    }
}
