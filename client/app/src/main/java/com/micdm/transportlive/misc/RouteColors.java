package com.micdm.transportlive.misc;

import android.graphics.Color;

import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;

import java.util.Hashtable;
import java.util.Map;

public class RouteColors {

    private final Map<Route, Integer> colors;

    public RouteColors(Service service) {
        this.colors = getColors(service);
    }

    private Map<Route, Integer> getColors(Service service) {
        Map<Route, Integer> colors = new Hashtable<Route, Integer>();
        int count = getRouteCount(service);
        int number = 0;
        for (Transport transport: service.transports) {
            for (Route route: transport.routes) {
                colors.put(route, getColor(count, number));
                number += 1;
            }
        }
        return colors;
    }

    private int getRouteCount(Service service) {
        int count = 0;
        for (Transport transport: service.transports) {
            count += transport.routes.size();
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
