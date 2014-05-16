package com.micdm.transportlive.server;

import android.content.Context;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.VehicleInfo;
import com.micdm.transportlive.server.commands.Command;
import com.micdm.transportlive.server.commands.GetForecastCommand;
import com.micdm.transportlive.server.commands.GetVehiclesCommand;

import java.util.List;

public class DataLoader {

    public static interface OnLoadVehiclesListener {
        public void onLoad(List<VehicleInfo> vehicles);
        public void onError();
    }

    public static interface OnLoadForecastListener {
        public void onLoad(Forecast forecast);
        public void onError();
    }

    public static class Task {

        private final ServerConnectTask task;

        public Task(ServerConnectTask task) {
            this.task = task;
        }

        public void cancel() {
            task.cancel(true);
        }
    }

    private final Context context;

    public DataLoader(Context context) {
        this.context = context;
    }

    public Task loadVehicles(List<SelectedRouteInfo> selected, final OnLoadVehiclesListener listener) {
        ServerConnectTask task = new ServerConnectTask(context, new ServerConnectTask.OnResultListener() {
            @Override
            public void onResult(Command.Result result) {
                List<VehicleInfo> vehicles = ((GetVehiclesCommand.Result) result).vehicles;
                listener.onLoad(vehicles);
            }
            @Override
            public void onError() {
                listener.onError();
            }
        });
        task.execute(new GetVehiclesCommand(selected));
        return new Task(task);
    }

    public Task loadForecast(Service service, SelectedStationInfo selected, final OnLoadForecastListener listener) {
        ServerConnectTask task = new ServerConnectTask(context, new ServerConnectTask.OnResultListener() {
            @Override
            public void onResult(Command.Result result) {
                Forecast forecast = ((GetForecastCommand.Result) result).forecast;
                listener.onLoad(forecast);
            }
            @Override
            public void onError() {
                listener.onError();
            }
        });
        task.execute(new GetForecastCommand(service, selected));
        return new Task(task);
    }
}
