package com.micdm.transportlive.server.handlers;

import android.content.Context;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.ForecastVehicle;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.server.commands.Command;
import com.micdm.transportlive.server.commands.GetForecastCommand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetForecastCommandHandler extends CommandHandler {

    private static final String API_METHOD = "forecasts";

    public GetForecastCommandHandler(Context context, Command command) {
        super(context, command);
    }

    @Override
    public GetForecastCommand.Result handle() {
        SelectedStationInfo selected = ((GetForecastCommand) command).selected;
        List<RequestParam> params = getRequestParams(selected);
        JSONObject response = sendRequest(API_METHOD, params);
        if (response == null) {
            return null;
        }
        Service service = ((GetForecastCommand) command).service;
        try {
            List<Forecast> forecasts = getForecasts(response.getJSONArray("stations"), service);
            return new GetForecastCommand.Result(forecasts.get(0));
        } catch (JSONException e) {
            return null;
        }
    }

    private List<RequestParam> getRequestParams(SelectedStationInfo selected) {
        List<RequestParam> params = new ArrayList<RequestParam>();
        params.add(new RequestParam("station", String.format("%s-%s", selected.transport.id, selected.station.id)));
        return params;
    }

    private List<Forecast> getForecasts(JSONArray forecastListJson, Service service) throws JSONException {
        List<Forecast> forecasts = new ArrayList<Forecast>();
        for (int i = 0; i < forecastListJson.length(); i += 1) {
            JSONObject forecastJson = forecastListJson.getJSONObject(i);
            forecasts.add(getForecast(forecastJson, service));
        }
        return forecasts;
    }

    private Forecast getForecast(JSONObject forecastJson, Service service) throws JSONException {
        Forecast forecast = new Forecast();
        List<ForecastVehicle> vehicles = getForecastVehicles(forecastJson.getJSONArray("vehicles"), service);
        Collections.copy(forecast.vehicles, vehicles);
        return forecast;
    }

    private List<ForecastVehicle> getForecastVehicles(JSONArray vehicleListJson, Service service) throws JSONException {
        List<ForecastVehicle> vehicles = new ArrayList<ForecastVehicle>();
        for (int i = 0; i < vehicleListJson.length(); i += 1) {
            JSONObject vehicleJson = vehicleListJson.getJSONObject(i);
            vehicles.add(getForecastVehicle(vehicleJson, service));
        }
        return vehicles;
    }

    private ForecastVehicle getForecastVehicle(JSONObject vehicleJson, Service service) throws JSONException {
        Transport transport = service.getTransportById(vehicleJson.getInt("transport"));
        Route route = transport.getRouteByNumber(vehicleJson.getInt("route"));
        int time = vehicleJson.getInt("time");
        return new ForecastVehicle(transport, route, time);
    }
}
