package com.micdm.transportlive.server.handlers;

import android.content.Context;

import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.RouteInfo;
import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.data.Vehicle;
import com.micdm.transportlive.server.commands.Command;
import com.micdm.transportlive.server.commands.GetVehiclesCommand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GetVehiclesCommandHandler extends CommandHandler {

    private static final String API_METHOD = "vehicles";

    public GetVehiclesCommandHandler(Context context, Command command) {
        super(context, command);
    }

    @Override
    public GetVehiclesCommand.Result handle() {
        List<SelectedRouteInfo> selected = ((GetVehiclesCommand) command).selected;
        List<RequestParam> params = getRequestParams(selected);
        JSONObject response = sendRequest(API_METHOD, params);
        if (response == null) {
            return null;
        }
        Service service = ((GetVehiclesCommand) command).service;
        try {
            List<RouteInfo> routes = getRoutesInfo(response.getJSONArray("result"), service);
            return new GetVehiclesCommand.Result(routes);
        } catch (JSONException e) {
            return null;
        }
    }

    private List<RequestParam> getRequestParams(List<SelectedRouteInfo> selected) {
        List<RequestParam> params = new ArrayList<RequestParam>();
        for (SelectedRouteInfo info: selected) {
            params.add(new RequestParam("route", String.format("%s-%s", info.transport.id, info.route.number)));
        }
        return params;
    }

    private List<RouteInfo> getRoutesInfo(JSONArray routeListJson, Service service) throws JSONException {
        List<RouteInfo> routes = new ArrayList<RouteInfo>();
        for (int i = 0; i < routeListJson.length(); i += 1) {
            JSONObject routeJson = routeListJson.getJSONObject(i);
            routes.add(getRouteInfo(routeJson, service));
        }
        return routes;
    }

    private RouteInfo getRouteInfo(JSONObject routeJson, Service service) throws JSONException {
        Transport transport = service.getTransportById(routeJson.getInt("transport"));
        Route route = transport.getRouteByNumber(routeJson.getInt("route"));
        RouteInfo info = new RouteInfo(transport, route);
        for (Vehicle vehicle: getVehicles(routeJson.getJSONArray("vehicles"))) {
            info.vehicles.add(vehicle);
        }
        return info;
    }

    private List<Vehicle> getVehicles(JSONArray vehicleListJson) throws JSONException {
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        for (int i = 0; i < vehicleListJson.length(); i += 1) {
            JSONObject vehicleJson = vehicleListJson.getJSONObject(i);
            vehicles.add(getVehicle(vehicleJson));
        }
        return vehicles;
    }

    private Vehicle getVehicle(JSONObject vehicleJson) throws JSONException {
        String number = vehicleJson.getString("number");
        BigDecimal latitude = new BigDecimal(vehicleJson.getString("lat"));
        BigDecimal longitude = new BigDecimal(vehicleJson.getString("lon"));
        int course = vehicleJson.getInt("course");
        return new Vehicle(number, latitude, longitude, course);
    }
}
