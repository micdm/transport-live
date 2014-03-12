package com.micdm.transportlive.misc;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.micdm.transportlive.server.ServerConnectTask;
import com.micdm.transportlive.server.commands.Command;

import java.util.ArrayList;

public class TaskGroupExecutor {

    public static interface OnExecuteListener {
        public void onExecute(Command.Result[] results);
    }

    private Context context;
    private ArrayList<Command.Result> results = new ArrayList<Command.Result>();

    public TaskGroupExecutor(Context context) {
        this.context = context;
    }

    public void execute(final Command[] commands, final OnExecuteListener callback) {
        for (Command command: commands) {
            ServerConnectTask task = new ServerConnectTask(context, new ServerConnectTask.OnResultListener() {
                @Override
                public void onResult(Command.Result result) {
                    results.add(result);
                    if (results.size() == commands.length) {
                        callback.onExecute(results.toArray(new Command.Result[results.size()]));
                    }
                }
            });
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                executeModernWay(task, command);
            } else {
                executeOldWay(task, command);
            }
        }
    }

    private void executeOldWay(ServerConnectTask task, Command command) {
        task.execute(command);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void executeModernWay(ServerConnectTask task, Command command) {
        task.executeOnExecutor(ServerConnectTask.THREAD_POOL_EXECUTOR, command);
    }
}
