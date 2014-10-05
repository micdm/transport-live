package com.micdm.transportlive.server.handlers;

import android.content.Context;

import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.RoutePopulation;
import com.micdm.transportlive.data.SelectedRoute;
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
        List<SelectedRoute> selected = ((GetVehiclesCommand) command).selected;
        List<RequestParam> params = getRequestParams(selected);
        JSONObject response = sendRequest(API_METHOD, params);
        if (response == null) {
            return null;
        }
        Service service = ((GetVehiclesCommand) command).service;
        try {
            List<RoutePopulation> routes = getRoutePopulations(response.getJSONArray("result"), service);
            return new GetVehiclesCommand.Result(routes);
        } catch (JSONException e) {
            return null;
        }
    }

    private List<RequestParam> getRequestParams(List<SelectedRoute> selected) {
        List<RequestParam> params = new ArrayList<RequestParam>();
        for (SelectedRoute route: selected) {
            params.add(new RequestParam("route", String.format("%s-%s", route.getTransportId(), route.getRouteNumber())));
        }
        return params;
    }

    private List<RoutePopulation> getRoutePopulations(JSONArray populationsJson, Service service) throws JSONException {
        List<RoutePopulation> populations = new ArrayList<RoutePopulation>();
        for (int i = 0; i < populationsJson.length(); i += 1) {
            JSONObject populationJson = populationsJson.getJSONObject(i);
            populations.add(getRouteInfo(populationJson, service));
        }
        return populations;
    }

    private RoutePopulation getRouteInfo(JSONObject routeJson, Service service) throws JSONException {
        Transport transport = service.getTransportById(routeJson.getInt("transport"));
        Route route = transport.getRouteByNumber(routeJson.getInt("route"));
        List<Vehicle> vehicles = getVehicles(routeJson.getJSONArray("vehicles"));
        return new RoutePopulation(transport.getId(), route.getNumber(), vehicles);
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
