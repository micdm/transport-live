package com.micdm.transportlive.misc;

import android.content.Context;

import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ServiceCache {

    private static final String CACHE_FILE_NAME = "service.json";

    public static ServiceCache getInstance(Context context) {
        return new ServiceCache(context);
    }

    private Context context;

    private ServiceCache(Context context) {
        this.context = context;
    }

    public Service get() {
        try {
            JSONObject json = load();
            if (json == null) {
                return null;
            }
            return jsonToService(json);
        } catch (JSONException e) {
            return null;
        }
    }

    private JSONObject load() {
        try {
            FileInputStream input = context.openFileInput(CACHE_FILE_NAME);
            String content = IOUtils.toString(input);
            return (JSONObject)new JSONTokener(content).nextValue();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (JSONException e) {
            return null;
        }
    }

    private Service jsonToService(JSONObject json) throws JSONException {
        Service service = new Service();
        JSONArray transportJson = json.getJSONArray("transports");
        for (int i = 0; i < transportJson.length(); i += 1) {
            service.transports.add(jsonToTransport(transportJson.getJSONObject(i)));
        }
        return service;
    }

    private Transport jsonToTransport(JSONObject json) throws JSONException {
        Transport transport = new Transport(json.getInt("id"), getTransportType(json.getString("type")));
        JSONArray routeJson = json.getJSONArray("routes");
        for (int i = 0; i < routeJson.length(); i += 1) {
            transport.routes.add(jsonToRoute(routeJson.getJSONObject(i)));
        }
        return transport;
    }

    private Transport.Type getTransportType(String value) {
        for (Transport.Type type: Transport.Type.values()) {
            if (value.equals(type.toString())) {
                return type;
            }
        }
        throw new RuntimeException("unknown transport type");
    }

    private Route jsonToRoute(JSONObject json) throws JSONException {
        Route route = new Route(json.getInt("number"), json.getBoolean("isChecked"));
        JSONArray directionJson = json.getJSONArray("directions");
        for (int i = 0; i < directionJson.length(); i += 1) {
            route.directions.add(jsonToDirection(directionJson.getJSONObject(i)));
        }
        return route;
    }

    private Direction jsonToDirection(JSONObject json) throws JSONException {
        return new Direction(json.getInt("id"), json.getString("start"), json.getString("finish"));
    }

    public void set(Service service) {
        try {
            save(serviceToJson(service));
        } catch (JSONException e) {

        }
    }

    private void save(JSONObject object) {
        try {
            FileOutputStream output = context.openFileOutput(CACHE_FILE_NAME, Context.MODE_PRIVATE);
            output.write(object.toString().getBytes());
            output.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    private JSONObject serviceToJson(Service service) throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray transportJson = new JSONArray();
        for (Transport transport: service.transports) {
            transportJson.put(transportToJson(transport));
        }
        json.put("transports", transportJson);
        return json;
    }

    private JSONObject transportToJson(Transport transport) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", transport.id);
        json.put("type", transport.type.toString());
        JSONArray routeJson = new JSONArray();
        for (Route route: transport.routes) {
            routeJson.put(routeToJson(route));
        }
        json.put("routes", routeJson);
        return json;
    }

    private JSONObject routeToJson(Route route) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("number", route.number);
        json.put("isChecked", route.isChecked);
        JSONArray directionJson = new JSONArray();
        for (Direction direction: route.directions) {
            directionJson.put(directionToJson(direction));
        }
        json.put("directions", directionJson);
        return json;
    }

    private JSONObject directionToJson(Direction direction) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", direction.id);
        json.put("start", direction.start);
        json.put("finish", direction.finish);
        return json;
    }
}
