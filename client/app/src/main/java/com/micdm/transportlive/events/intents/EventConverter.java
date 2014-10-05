package com.micdm.transportlive.events.intents;

import android.content.Context;
import android.content.Intent;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.RoutePopulation;
import com.micdm.transportlive.data.SelectedRoute;
import com.micdm.transportlive.data.SelectedStation;
import com.micdm.transportlive.donate.DonateProduct;
import com.micdm.transportlive.donate.DonateProductParcel;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;
import com.micdm.transportlive.events.events.LoadDonateProductsEvent;
import com.micdm.transportlive.events.events.LoadForecastsEvent;
import com.micdm.transportlive.events.events.LoadRoutesEvent;
import com.micdm.transportlive.events.events.LoadServiceEvent;
import com.micdm.transportlive.events.events.LoadStationsEvent;
import com.micdm.transportlive.events.events.LoadVehiclesEvent;
import com.micdm.transportlive.events.events.RequestDonateEvent;
import com.micdm.transportlive.events.events.RequestSelectRouteEvent;
import com.micdm.transportlive.events.events.RequestSelectStationEvent;
import com.micdm.transportlive.events.events.RequestUnselectRouteEvent;
import com.micdm.transportlive.events.events.RequestUnselectStationEvent;
import com.micdm.transportlive.parcels.ForecastParcel;
import com.micdm.transportlive.parcels.RoutePopulationParcel;
import com.micdm.transportlive.parcels.SelectedRouteParcel;
import com.micdm.transportlive.parcels.SelectedStationParcel;
import com.micdm.transportlive.parcels.ServiceParcel;

import java.util.ArrayList;
import java.util.List;

public class EventConverter {

    private final Context context;

    public EventConverter(Context context) {
        this.context = context;
    }

    public Intent convert(Event event) {
        Intent intent = new Intent(getIntentAction(event.getType()));
        switch (event.getType()) {
            case LOAD_SERVICE:
                buildIntentForLoadServiceEvent((LoadServiceEvent) event, intent);
                break;
            case LOAD_ROUTES:
                buildIntentForLoadRoutesEvent((LoadRoutesEvent) event, intent);
                break;
            case REQUEST_SELECT_ROUTE:
                buildIntentForRequestSelectRouteEvent((RequestSelectRouteEvent) event, intent);
                break;
            case REQUEST_UNSELECT_ROUTE:
                buildIntentForRequestUnselectRouteEvent((RequestUnselectRouteEvent) event, intent);
                break;
            case LOAD_VEHICLES:
                buildIntentForLoadVehiclesEvent((LoadVehiclesEvent) event, intent);
                break;
            case LOAD_STATIONS:
                buildIntentForLoadStationsEvent((LoadStationsEvent) event, intent);
                break;
            case REQUEST_SELECT_STATION:
                buildIntentForRequestSelectStationEvent((RequestSelectStationEvent) event, intent);
                break;
            case REQUEST_UNSELECT_STATION:
                buildIntentForRequestUnselectStationEvent((RequestUnselectStationEvent) event, intent);
                break;
            case LOAD_FORECASTS:
                buildIntentForLoadForecastsEvent((LoadForecastsEvent) event, intent);
                break;
            case LOAD_DONATE_PRODUCTS:
                buildIntentForLoadDonateProductsEvent((LoadDonateProductsEvent) event, intent);
                break;
            case REQUEST_DONATE:
                buildIntentForRequestDonateEvent((RequestDonateEvent) event, intent);
                break;
        }
        return intent;
    }

    public String getIntentAction(EventType type) {
        return String.format("%s.event.%s", context.getPackageName(), type);
    }

    private void buildIntentForLoadServiceEvent(LoadServiceEvent event, Intent intent) {
        intent.putExtra("service", new ServiceParcel(event.getService()));
    }

    private void buildIntentForLoadRoutesEvent(LoadRoutesEvent event, Intent intent) {
        ArrayList<SelectedRouteParcel> parcels = new ArrayList<SelectedRouteParcel>();
        for (SelectedRoute route: event.getRoutes()) {
            parcels.add(new SelectedRouteParcel(route));
        }
        intent.putParcelableArrayListExtra("routes", parcels);
    }

    private void buildIntentForRequestSelectRouteEvent(RequestSelectRouteEvent event, Intent intent) {
        intent.putExtra("route", new SelectedRouteParcel(event.getRoute()));
    }

    private void buildIntentForRequestUnselectRouteEvent(RequestUnselectRouteEvent event, Intent intent) {
        intent.putExtra("route", new SelectedRouteParcel(event.getRoute()));
    }

    private void buildIntentForLoadVehiclesEvent(LoadVehiclesEvent event, Intent intent) {
        intent.putExtra("state", event.getState());
        List<RoutePopulation> vehicles = event.getPopulations();
        ArrayList<RoutePopulationParcel> parcels;
        if (vehicles == null) {
            parcels = null;
        } else {
            parcels = new ArrayList<RoutePopulationParcel>();
            for (RoutePopulation vehicle: event.getPopulations()) {
                parcels.add(new RoutePopulationParcel(vehicle));
            }
        }
        intent.putParcelableArrayListExtra("vehicles", parcels);
    }

    private void buildIntentForLoadStationsEvent(LoadStationsEvent event, Intent intent) {
        ArrayList<SelectedStationParcel> parcels = new ArrayList<SelectedStationParcel>();
        for (SelectedStation station: event.getStations()) {
            parcels.add(new SelectedStationParcel(station));
        }
        intent.putParcelableArrayListExtra("stations", parcels);
    }

    private void buildIntentForRequestSelectStationEvent(RequestSelectStationEvent event, Intent intent) {
        intent.putExtra("station", new SelectedStationParcel(event.getStation()));
    }

    private void buildIntentForRequestUnselectStationEvent(RequestUnselectStationEvent event, Intent intent) {
        intent.putExtra("station", new SelectedStationParcel(event.getStation()));
    }

    private void buildIntentForLoadForecastsEvent(LoadForecastsEvent event, Intent intent) {
        intent.putExtra("state", event.getState());
        List<Forecast> forecasts = event.getForecasts();
        ArrayList<ForecastParcel> parcels;
        if (forecasts == null) {
            parcels = null;
        } else {
            parcels = new ArrayList<ForecastParcel>();
            for (Forecast forecast: event.getForecasts()) {
                parcels.add(new ForecastParcel(forecast));
            }
        }
        intent.putParcelableArrayListExtra("forecasts", parcels);
    }

    private void buildIntentForLoadDonateProductsEvent(LoadDonateProductsEvent event, Intent intent) {
        ArrayList<DonateProductParcel> parcels = new ArrayList<DonateProductParcel>();
        for (DonateProduct product: event.getProducts()) {
            parcels.add(new DonateProductParcel(product));
        }
        intent.putParcelableArrayListExtra("products", parcels);
    }

    private void buildIntentForRequestDonateEvent(RequestDonateEvent event, Intent intent) {
        intent.putExtra("product", new DonateProductParcel(event.getProduct()));
    }
}
