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

    public interface OnLoadListener {
        public void onLoad(Service service);
        public void onNoConnection();
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public static void loadTransports(Context context, Service service, final OnLoadListener callback) {
        if (!isNetworkAvailable(context)) {
            callback.onNoConnection();
        } else {
            ServerConnectTask task = new ServerConnectTask(new ServerConnectTask.OnResultListener() {
                @Override
                public void onResult(Command.Result result) {
                    Service service = ((GetTransportsCommand.Result)result).service;
                    callback.onLoad(service);
                }
            });
            task.execute(new GetTransportsCommand(service));
        }
    }

    public static void loadRoutes(Context context, Service service, final OnLoadListener callback) {
        if (!isNetworkAvailable(context)) {
            callback.onNoConnection();
        } else {
            ServerConnectTask task = new ServerConnectTask(new ServerConnectTask.OnResultListener() {
                @Override
                public void onResult(Command.Result result) {
                    Service service = ((GetRoutesCommand.Result)result).service;
                    callback.onLoad(service);
                }
            });
            task.execute(new GetRoutesCommand(service));
        }
    }

    public static void loadPoints(Context context, Service service, final OnLoadListener callback) {
        if (!isNetworkAvailable(context)) {
            callback.onNoConnection();
        } else {
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
                    callback.onLoad(service);
                }
            });
        }
    }

    public static void loadStations(Context context, Service service, final OnLoadListener callback) {
        if (!isNetworkAvailable(context)) {
            callback.onNoConnection();
        } else {
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
    }

    public static void loadVehicles(Context context, Service service, final OnLoadListener callback) {
        if (!isNetworkAvailable(context)) {
            callback.onNoConnection();
        } else {
            ServerConnectTask task = new ServerConnectTask(new ServerConnectTask.OnResultListener() {
                @Override
                public void onResult(Command.Result result) {
                    Service service = ((GetVehiclesCommand.Result)result).service;
                    callback.onLoad(service);
                }
            });
            task.execute(new GetVehiclesCommand(service));
        }
    }
}
