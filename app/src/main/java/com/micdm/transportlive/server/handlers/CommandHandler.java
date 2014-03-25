package com.micdm.transportlive.server.handlers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.AndroidHttpClient;

import com.micdm.transportlive.R;
import com.micdm.transportlive.server.cities.CityConfig;
import com.micdm.transportlive.server.commands.Command;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class CommandHandler {

    private Context context;
    private CityConfig city;
    protected Command command;

    public CommandHandler(Context context) {
        this.context = context;
    }

    public void setCity(CityConfig city) {
        this.city = city;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    protected String sendRequest(String method, HashMap<String, String> params) throws IOException {
        AndroidHttpClient client = AndroidHttpClient.newInstance(getUserAgent());
        HttpGet request = new HttpGet(getRequestUri(method, params));
        HttpResponse response = client.execute(request);
        String content = IOUtils.toString(response.getEntity().getContent());
        client.close();
        return content;
    }

    private String getUserAgent() {
        return context.getString(R.string.user_agent, getAppVersion(), context.getString(R.string.contact_email));
    }

    private String getAppVersion() {
        try {
            PackageManager manager = context.getPackageManager();
            if (manager == null) {
                return "";
            }
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    private String getRequestUri(String method, HashMap<String, String> params) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http");
        builder.authority(city.backend.host);
        builder.path(String.format(city.backend.path, method));
        builder.appendQueryParameter("city", city.id);
        if (params != null) {
            for (Map.Entry<String, String> item: params.entrySet()) {
                builder.appendQueryParameter(item.getKey(), item.getValue());
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
