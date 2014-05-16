package com.micdm.transportlive.server;

import android.content.Context;
import android.os.AsyncTask;

import com.micdm.transportlive.server.commands.Command;
import com.micdm.transportlive.server.commands.GetForecastCommand;
import com.micdm.transportlive.server.commands.GetVehiclesCommand;
import com.micdm.transportlive.server.handlers.CommandHandler;
import com.micdm.transportlive.server.handlers.GetForecastCommandHandler;
import com.micdm.transportlive.server.handlers.GetVehiclesCommandHandler;

public class ServerConnectTask extends AsyncTask<Command, Void, Command.Result> {

    public static interface OnResultListener {
        public void onResult(Command.Result result);
        public void onError();
    }

    private Context context;
    private OnResultListener listener;

    public ServerConnectTask(Context context, OnResultListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected Command.Result doInBackground(Command... commands) {
        return executeCommand(commands[0]);
    }

    private Command.Result executeCommand(Command command) {
        CommandHandler handler = getCommandHandler(command);
        return handler.handle();
    }

    private CommandHandler getCommandHandler(Command command) {
        if (command instanceof GetVehiclesCommand) {
            return new GetVehiclesCommandHandler(context, command);
        }
        if (command instanceof GetForecastCommand) {
            return new GetForecastCommandHandler(context, command);
        }
        throw new RuntimeException("unknown command");
    }

    @Override
    protected void onPostExecute(Command.Result result) {
        if (result == null) {
            listener.onError();
        } else {
            listener.onResult(result);
        }
    }
}
