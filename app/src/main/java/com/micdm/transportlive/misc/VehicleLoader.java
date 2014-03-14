package com.micdm.transportlive.misc;

import android.content.Context;

import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.server.ServerConnectTask;
import com.micdm.transportlive.server.commands.Command;
import com.micdm.transportlive.server.commands.GetVehiclesCommand;

import java.util.List;

public class VehicleLoader {

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

    public VehicleLoader(Context context) {
        this.context = context;
    }

    public Task load(Service service, List<SelectedRouteInfo> selected, final OnLoadListener listener) {
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
