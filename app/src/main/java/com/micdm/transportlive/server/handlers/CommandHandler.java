package com.micdm.transportlive.server.handlers;

import android.net.Uri;
import android.net.http.AndroidHttpClient;

import com.micdm.transportlive.server.cities.CityConfig;
import com.micdm.transportlive.server.commands.Command;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class CommandHandler {

    protected static enum Backend {
        FIRST,
        SECOND
    }

    private static final String USER_AGENT = "transportlive";
    private static final String CITY_KEY = "ryazan";// "tomsk";

    protected Backend backend;
    protected CityConfig city;
    protected Command command;

    public CommandHandler(Backend backend) {
        this.backend = backend;
    }

    public void setCity(CityConfig city) {
        this.city = city;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    protected String sendRequest(String method, HashMap<String, String> params) {
        try {
            AndroidHttpClient client = AndroidHttpClient.newInstance(USER_AGENT);
            HttpGet request = new HttpGet(getRequestUri(method, params));
            HttpResponse response = client.execute(request);
            String content = IOUtils.toString(response.getEntity().getContent());
            client.close();
            return content;
        } catch (IOException e) {
            throw new RuntimeException("can't send request");
        }
    }

    protected String sendRequest(String method) {
        return sendRequest(method, null);
    }

    private String getRequestUri(String method, HashMap<String, String> params) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http");
        switch (backend) {
            case FIRST:
                builder.authority(city.firstBackend.host);
                builder.path(String.format(city.firstBackend.path, method));
                break;
            case SECOND:
                builder.authority(city.secondBackend.host);
                builder.path(String.format(city.secondBackend.path, method));
                break;
            default:
                throw new RuntimeException("unknown backend");
        }
        builder.appendQueryParameter("city", city.id);
        if (params != null) {
            for (Map.Entry<String, String> item: params.entrySet()) {
                builder.appendQueryParameter(item.getKey(), item.getValue());
            }
        }
        return builder.build().toString();
    }

    public abstract Command.Result handle();
}
