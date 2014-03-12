package com.micdm.transportlive.misc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.server.ServerConnectTask;
import com.micdm.transportlive.server.commands.Command;
import com.micdm.transportlive.server.commands.GetPointsCommand;
import com.micdm.transportlive.server.commands.GetRoutesCommand;
import com.micdm.transportlive.server.commands.GetStationsCommand;
import com.micdm.transportlive.server.commands.GetTransportsCommand;
import com.micdm.transportlive.server.commands.GetVehiclesCommand;

import java.util.ArrayList;

public class ServiceLoader {

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

    public ServiceLoader(Context context, OnNoConnectionListener noConnectionListener) {
        this.context = context;
        this.noConnectionListener = noConnectionListener;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public void loadTransports(Service service, final OnLoadListener listener) {
        if (!isNetworkAvailable()) {
            noConnectionListener.onNoConnection();
        } else {
            ServerConnectTask task = new ServerConnectTask(context, new ServerConnectTask.OnResultListener() {
                @Override
                public void onResult(Command.Result result) {
                    Service service = ((GetTransportsCommand.Result)result).service;
                    listener.onLoad(service);
                }
            });
            task.execute(new GetTransportsCommand(service));
        }
    }

    public void loadRoutes(Service service, final OnLoadListener listener) {
        if (!isNetworkAvailable()) {
            noConnectionListener.onNoConnection();
        } else {
            ServerConnectTask task = new ServerConnectTask(context, new ServerConnectTask.OnResultListener() {
                @Override
                public void onResult(Command.Result result) {
                    Service service = ((GetRoutesCommand.Result)result).service;
                    listener.onLoad(service);
                }
            });
            task.execute(new GetRoutesCommand(service));
        }
    }

    public void loadPoints(Service service, final OnLoadListener listener) {
        if (!isNetworkAvailable()) {
            noConnectionListener.onNoConnection();
        } else {
            ArrayList<Command> commands = new ArrayList<Command>();
            for (Transport transport: service.transports) {
                for (Route route: transport.routes) {
                    commands.add(new GetPointsCommand(service, transport, route));
                }
            }
            TaskGroupExecutor executor = new TaskGroupExecutor(context);
            executor.execute(commands.toArray(new Command[commands.size()]), new TaskGroupExecutor.OnExecuteListener() {
                @Override
                public void onExecute(Command.Result[] results) {
                    Service service = ((GetPointsCommand.Result)results[0]).service;
                    listener.onLoad(service);
                }
            });
        }
    }

    public void loadStations(Service service, final OnLoadListener listener) {
        if (!isNetworkAvailable()) {
            noConnectionListener.onNoConnection();
        } else {
            ArrayList<Command> commands = new ArrayList<Command>();
            for (Transport transport: service.transports) {
                for (Route route: transport.routes) {
                    commands.add(new GetStationsCommand(service, transport, route));
                }
            }
            TaskGroupExecutor executor = new TaskGroupExecutor(context);
            executor.execute(commands.toArray(new Command[commands.size()]), new TaskGroupExecutor.OnExecuteListener() {
                @Override
                public void onExecute(Command.Result[] results) {
                    Service service = ((GetStationsCommand.Result)results[0]).service;
                    listener.onLoad(service);
                }
            });
        }
    }

    public Task loadVehicles(Service service, final OnLoadListener listener) {
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
        task.execute(new GetVehiclesCommand(service));
        return new Task(task);
    }
}
