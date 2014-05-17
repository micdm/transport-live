package com.micdm.transportlive.server.handlers;

import android.content.Context;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.ForecastVehicle;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Station;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.server.commands.Command;
import com.micdm.transportlive.server.commands.GetForecastsCommand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetForecastsCommandHandler extends CommandHandler {

    private static final String API_METHOD = "forecasts";

    public GetForecastsCommandHandler(Context context, Command command) {
        super(context, command);
    }

    @Override
    public GetForecastsCommand.Result handle() {
        List<SelectedStationInfo> selected = ((GetForecastsCommand) command).selected;
        List<RequestParam> params = getRequestParams(selected);
        JSONObject response = sendRequest(API_METHOD, params);
        if (response == null) {
            return null;
        }
        Service service = ((GetForecastsCommand) command).service;
        try {
            List<Forecast> forecasts = getForecasts(response.getJSONArray("result"), service);
            return new GetForecastsCommand.Result(forecasts);
        } catch (JSONException e) {
            return null;
        }
    }

    private List<RequestParam> getRequestParams(List<SelectedStationInfo> selected) {
        List<RequestParam> params = new ArrayList<RequestParam>();
        for (SelectedStationInfo info: selected) {
            params.add(new RequestParam("station", String.format("%s-%s", info.transport.id, info.station.id)));
        }
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
        Transport transport = service.getTransportById(forecastJson.getInt("transport"));
        Station station = transport.getStationById(forecastJson.getInt("station"));
        Forecast forecast = new Forecast(transport, station);
        for (ForecastVehicle vehicle: getForecastVehicles(forecastJson.getJSONArray("vehicles"), transport)) {
            forecast.vehicles.add(vehicle);
        }
        return forecast;
    }

    private List<ForecastVehicle> getForecastVehicles(JSONArray vehicleListJson, Transport transport) throws JSONException {
        List<ForecastVehicle> vehicles = new ArrayList<ForecastVehicle>();
        for (int i = 0; i < vehicleListJson.length(); i += 1) {
            JSONObject vehicleJson = vehicleListJson.getJSONObject(i);
            vehicles.add(getForecastVehicle(vehicleJson, transport));
        }
        return vehicles;
    }

    private ForecastVehicle getForecastVehicle(JSONObject vehicleJson, Transport transport) throws JSONException {
        Route route = transport.getRouteByNumber(vehicleJson.getInt("route"));
        int time = vehicleJson.getInt("time");
        return new ForecastVehicle(route, time);
    }
}
