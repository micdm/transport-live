package com.micdm.transportlive.server;

import android.content.Context;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.RouteInfo;
import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.server.commands.Command;
import com.micdm.transportlive.server.commands.GetForecastsCommand;
import com.micdm.transportlive.server.commands.GetVehiclesCommand;

import java.util.List;

public class DataLoader {

    public static interface OnLoadVehiclesListener {
        public void onLoad(List<RouteInfo> vehicles);
        public void onError();
    }

    public static interface OnLoadForecastListener {
        public void onLoad(List<Forecast> forecasts);
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

    public Task loadVehicles(Service service, List<SelectedRouteInfo> selected, final OnLoadVehiclesListener listener) {
        ServerConnectTask task = new ServerConnectTask(context, new ServerConnectTask.OnResultListener() {
            @Override
            public void onResult(Command.Result result) {
                List<RouteInfo> routes = ((GetVehiclesCommand.Result) result).routes;
                listener.onLoad(routes);
            }
            @Override
            public void onError() {
                listener.onError();
            }
        });
        task.execute(new GetVehiclesCommand(service, selected));
        return new Task(task);
    }

    public Task loadForecast(Service service, List<SelectedStationInfo> selected, final OnLoadForecastListener listener) {
        ServerConnectTask task = new ServerConnectTask(context, new ServerConnectTask.OnResultListener() {
            @Override
            public void onResult(Command.Result result) {
                List<Forecast> forecasts = ((GetForecastsCommand.Result) result).forecasts;
                listener.onLoad(forecasts);
            }
            @Override
            public void onError() {
                listener.onError();
            }
        });
        task.execute(new GetForecastsCommand(service, selected));
        return new Task(task);
    }
}
