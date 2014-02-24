package com.micdm.transportlive.server;

import android.os.AsyncTask;

import com.micdm.transportlive.server.cities.CityConfig;
import com.micdm.transportlive.server.commands.Command;
import com.micdm.transportlive.server.commands.GetPointsCommand;
import com.micdm.transportlive.server.commands.GetRoutesCommand;
import com.micdm.transportlive.server.commands.GetStationsCommand;
import com.micdm.transportlive.server.commands.GetTransportsCommand;
import com.micdm.transportlive.server.commands.GetVehiclesCommand;
import com.micdm.transportlive.server.handlers.CommandHandler;
import com.micdm.transportlive.server.handlers.GetPointsCommandHandler;
import com.micdm.transportlive.server.handlers.GetRoutesCommandHandler;
import com.micdm.transportlive.server.handlers.GetStationsCommandHandler;
import com.micdm.transportlive.server.handlers.GetTransportsCommandHandler;
import com.micdm.transportlive.server.handlers.GetVehiclesCommandHandler;

public class ServerConnectTask extends AsyncTask<Command, Void, Command.Result> {

    public static interface OnResultListener {
        public void onResult(Command.Result result);
    }

    private OnResultListener callback;

    public ServerConnectTask(OnResultListener callback) {
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
        if (command instanceof GetTransportsCommand) {
            return new GetTransportsCommandHandler();
        }
        if (command instanceof GetRoutesCommand) {
            return new GetRoutesCommandHandler();
        }
        if (command instanceof GetStationsCommand) {
            return new GetStationsCommandHandler();
        }
        if (command instanceof GetPointsCommand) {
            return new GetPointsCommandHandler();
        }
        if (command instanceof GetVehiclesCommand) {
            return new GetVehiclesCommandHandler();
        }
        throw new RuntimeException("unknown command");
    }

    @Override
    protected void onPostExecute(Command.Result result) {
        callback.onResult(result);
    }
}
