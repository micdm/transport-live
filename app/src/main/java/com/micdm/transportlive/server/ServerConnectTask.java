package com.micdm.transportlive.server;

import android.content.Context;
import android.os.AsyncTask;

import com.micdm.transportlive.server.cities.CityConfig;
import com.micdm.transportlive.server.commands.Command;
import com.micdm.transportlive.server.commands.GetVehiclesCommand;
import com.micdm.transportlive.server.handlers.CommandHandler;
import com.micdm.transportlive.server.handlers.GetVehiclesCommandHandler;

public class ServerConnectTask extends AsyncTask<Command, Void, Command.Result> {

    public static interface OnResultListener {
        public void onResult(Command.Result result);
    }

    private Context context;
    private OnResultListener callback;

    public ServerConnectTask(Context context, OnResultListener callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected Command.Result doInBackground(Command... commands) {
        return executeCommand(commands[0]);
    }

    private Command.Result executeCommand(Command command) {
        CommandHandler handler = getCommandHandler(command);
        handler.setCity(CityConfig.CITY_TOMSK);
        handler.setCommand(command);
        return handler.handle();
    }

    private CommandHandler getCommandHandler(Command command) {
        if (command instanceof GetVehiclesCommand) {
            return new GetVehiclesCommandHandler(context);
        }
        throw new RuntimeException("unknown command");
    }

    @Override
    protected void onPostExecute(Command.Result result) {
        callback.onResult(result);
    }
}
