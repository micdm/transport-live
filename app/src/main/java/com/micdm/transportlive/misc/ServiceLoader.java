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

    private OnLoadListener listener;

    public ServiceLoader(OnLoadListener listener) {
        this.listener = listener;
    }

    public void load() {
        Service service = new Service();
        loadTransports(service);
    }

    private void loadTransports(Service service) {
        ServerConnectTask task = new ServerConnectTask(new ServerConnectTask.OnResultListener() {
            @Override
            public void onResult(Command.Result result) {
                loadRoutes(((GetTransportsCommand.Result)result).service);
            }
        });
        task.execute(new GetTransportsCommand(service));
    }

    private void loadRoutes(final Service service) {
        ServerConnectTask task = new ServerConnectTask(new ServerConnectTask.OnResultListener() {
            @Override
            public void onResult(Command.Result result) {
                listener.onLoad(service);
            }
        });
        task.execute(new GetRoutesCommand(service));
    }
}
