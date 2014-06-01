package com.micdm.transportlive.server.handlers;

import android.content.Context;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Build;

import com.micdm.transportlive.R;
import com.micdm.transportlive.misc.Utils;
import com.micdm.transportlive.server.commands.Command;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public abstract class CommandHandler {

    protected static final class RequestParam {

        public final String name;
        public final String value;

        public RequestParam(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private static final String SERVER_PROTOCOL = "http";
    private static final String SERVER_HOST = "transport-live.tom.ru";
    private static final String SERVER_PATH = "/api/v1/%s";

    private final Context context;
    protected final Command command;

    public CommandHandler(Context context, Command command) {
        this.context = context;
        this.command = command;
    }

    protected JSONObject sendRequest(String method, List<RequestParam> params) {
        try {
            AndroidHttpClient client = AndroidHttpClient.newInstance(getUserAgent());
            HttpGet request = new HttpGet(getRequestUri(method, params));
            HttpResponse response = client.execute(request);
            String content = IOUtils.toString(response.getEntity().getContent());
            client.close();
            return new JSONObject(content);
        } catch (IOException e) {
            return null;
        } catch (JSONException e) {
            return null;
        }
    }

    private String getUserAgent() {
        return context.getString(R.string.user_agent, Utils.getAppVersion(context), Build.VERSION.RELEASE);
    }

    private String getRequestUri(String method, List<RequestParam> params) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SERVER_PROTOCOL);
        builder.authority(SERVER_HOST);
        builder.path(String.format(SERVER_PATH, method));
        if (params != null) {
            for (RequestParam param: params) {
                builder.appendQueryParameter(param.name, param.value);
            }
        }
        Uri uri = builder.build();
        if (uri == null) {
            throw new RuntimeException("cannot build URI");
        }
        return uri.toString();
    }

    public abstract Command.Result handle();
}
