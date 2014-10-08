package com.micdm.transportlive.misc;

import android.graphics.Color;

import com.micdm.transportlive.data.service.Route;
import com.micdm.transportlive.data.service.Service;
import com.micdm.transportlive.data.service.Transport;

import java.util.HashMap;
import java.util.Map;

public class RouteColors {

    private final Map<Route, Integer> colors;

    public RouteColors(Service service) {
        colors = getColors(service);
    }

    private Map<Route, Integer> getColors(Service service) {
        Map<Route, Integer> colors = new HashMap<Route, Integer>();
        int count = getRouteCount(service);
        int number = 0;
        for (Transport transport: service.getTransports()) {
            for (Route route: transport.getRoutes()) {
                colors.put(route, getColor(count, number));
                number += 1;
            }
        }
        return colors;
    }

    private int getRouteCount(Service service) {
        int count = 0;
        for (Transport transport: service.getTransports()) {
            count += transport.getRoutes().size();
        }
        return count;
    }

    private int getColor(int count, int number) {
        return Color.HSVToColor(new float[] {(255.0f / count) * number, 1f, 0.8f});
    }

    public int get(Route route) {
        return colors.get(route);
    }
}
