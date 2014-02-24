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

    public static interface OnLoadListener {
        public void onLoad(Object data);
        public void onNoConnection();
    }

    public static void loadService(Context context, OnLoadListener callback) {
        if (!isNetworkAvailable(context)) {
            callback.onNoConnection();
        } else {
            Service service = new Service();
            loadTransports(service, callback);
        }
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private static void loadTransports(Service service, final OnLoadListener callback) {
        ServerConnectTask task = new ServerConnectTask(new ServerConnectTask.OnResultListener() {
            @Override
            public void onResult(Command.Result result) {
                Service service = ((GetTransportsCommand.Result)result).service;
                loadRoutes(service, callback);
            }
        });
        task.execute(new GetTransportsCommand(service));
    }

    private static void loadRoutes(Service service, final OnLoadListener callback) {
        ServerConnectTask task = new ServerConnectTask(new ServerConnectTask.OnResultListener() {
            @Override
            public void onResult(Command.Result result) {
                Service service = ((GetRoutesCommand.Result)result).service;
                loadPoints(service, callback);
            }
        });
        task.execute(new GetRoutesCommand(service));
    }

    private static void loadPoints(Service service, final OnLoadListener callback) {
        ArrayList<Command> commands = new ArrayList<Command>();
        for (Transport transport: service.transports) {
            for (Route route: transport.routes) {
                commands.add(new GetPointsCommand(service, transport, route));
            }
        }
        TaskGroupExecutor executor = new TaskGroupExecutor();
        executor.execute(commands.toArray(new Command[commands.size()]), new TaskGroupExecutor.OnExecuteListener() {
            @Override
            public void onExecute(Command.Result[] results) {
                Service service = ((GetPointsCommand.Result)results[0]).service;
                loadStations(service, callback);
            }
        });
    }

    private static void loadStations(Service service, final OnLoadListener callback) {
        ArrayList<Command> commands = new ArrayList<Command>();
        for (Transport transport: service.transports) {
            for (Route route: transport.routes) {
                commands.add(new GetStationsCommand(service, transport, route));
            }
        }
        TaskGroupExecutor executor = new TaskGroupExecutor();
        executor.execute(commands.toArray(new Command[commands.size()]), new TaskGroupExecutor.OnExecuteListener() {
            @Override
            public void onExecute(Command.Result[] results) {
                Service service = ((GetStationsCommand.Result)results[0]).service;
                callback.onLoad(service);
            }
        });
    }

    public static void loadVehicles(Context context, Service service, final OnLoadListener callback) {
        if (!isNetworkAvailable(context)) {
            callback.onNoConnection();
        } else {
            ServerConnectTask task = new ServerConnectTask(new ServerConnectTask.OnResultListener() {
                @Override
                public void onResult(Command.Result result) {
                    callback.onLoad(((GetVehiclesCommand.Result)result).service);
                }
            });
            task.execute(new GetVehiclesCommand(service));
        }
    }
}
