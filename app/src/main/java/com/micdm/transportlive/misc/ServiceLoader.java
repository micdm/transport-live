package com.micdm.transportlive.misc;

import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.server.ServerConnectTask;
import com.micdm.transportlive.server.commands.Command;
import com.micdm.transportlive.server.commands.GetRoutesCommand;
import com.micdm.transportlive.server.commands.GetTransportsCommand;

public class ServiceLoader {

    public static interface OnLoadListener {
        public void onLoad(Service service);
    }

    public static void load(OnLoadListener callback) {
        Service service = new Service();
        loadTransports(service, callback);
    }

    private static void loadTransports(Service service, final OnLoadListener callback) {
        ServerConnectTask task = new ServerConnectTask(new ServerConnectTask.OnResultListener() {
            @Override
            public void onResult(Command.Result result) {
                loadRoutes(((GetTransportsCommand.Result)result).service, callback);
            }
        });
        task.execute(new GetTransportsCommand(service));
    }

    private static void loadRoutes(final Service service, final OnLoadListener callback) {
        ServerConnectTask task = new ServerConnectTask(new ServerConnectTask.OnResultListener() {
            @Override
            public void onResult(Command.Result result) {
                callback.onLoad(service);
            }
        });
        task.execute(new GetRoutesCommand(service));
    }
}
