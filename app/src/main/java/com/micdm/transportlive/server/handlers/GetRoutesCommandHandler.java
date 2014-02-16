package com.micdm.transportlive.server.handlers;

import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.server.commands.GetRoutesCommand;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class GetRoutesCommandHandler extends CommandHandler {

    public GetRoutesCommandHandler() {
        super(Backend.FIRST);
    }

    @Override
    public GetRoutesCommand.Result handle() {
        try {
            String response = sendRequest("searchAllRoutes");
            JSONObject routesJson = (JSONObject) new JSONTokener(response).nextValue();
            Service service = ((GetRoutesCommand)command).service;
            for (Transport transport: service.transports) {
                String description = routesJson.getString(getRouteDescriptionKey(transport.type));
                parseRoutes(transport, description);
            }
            return new GetRoutesCommand.Result(service);
        } catch (JSONException e) {
            throw new RuntimeException("can't parse JSON");
        }
    }

    private String getRouteDescriptionKey(Transport.Type type) {
        switch (type) {
            case BUS:
                return "Автобус";
            case TROLLEYBUS:
                return "Троллейбус";
            case TRAM:
                return "Трамвай";
            case TAXI:
                return "Маршрутное такси";
            default:
                throw new RuntimeException("unknown transport type");
        }
    }

    private void parseRoutes(Transport transport, String allRouteDescription) {
        for (String routeDescription: allRouteDescription.split("@ROUTE=")) {
            if (routeDescription.length() == 0) {
                continue;
            }
            transport.routes.add(parseRoute(routeDescription));
        }
    }

    private Route parseRoute(String description) {
        String[] chunks = description.split(";");
        Route route = new Route(Integer.valueOf(chunks[2]));
        route.directions.add(new Direction(Integer.valueOf(chunks[6])));
        route.directions.add(new Direction(Integer.valueOf(chunks[7])));
        return route;
    }
}
