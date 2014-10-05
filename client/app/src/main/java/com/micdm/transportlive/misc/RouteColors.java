package com.micdm.transportlive.misc;

import android.graphics.Color;

import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;

import java.util.HashMap;
import java.util.Map;

public class RouteColors {

    private final Map<Integer, Integer> colors;

    public RouteColors(Service service) {
        this.colors = getColors(service);
    }

    private Map<Integer, Integer> getColors(Service service) {
        Map<Integer, Integer> colors = new HashMap<Integer, Integer>();
        int count = getRouteCount(service);
        int number = 0;
        for (Transport transport: service.getTransports()) {
            for (Route route: transport.getRoutes()) {
                colors.put(route.getNumber(), getColor(count, number));
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

    public int get(int routeNumber) {
        return colors.get(routeNumber);
    }
}
