package com.micdm.transportlive.events.intents;

import android.content.Intent;
import android.os.Parcelable;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.RoutePopulation;
import com.micdm.transportlive.data.SelectedRoute;
import com.micdm.transportlive.data.SelectedStation;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.donate.DonateProduct;
import com.micdm.transportlive.donate.DonateProductParcel;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;
import com.micdm.transportlive.events.events.DonateEvent;
import com.micdm.transportlive.events.events.LoadDonateProductsEvent;
import com.micdm.transportlive.events.events.LoadForecastsEvent;
import com.micdm.transportlive.events.events.LoadRoutesEvent;
import com.micdm.transportlive.events.events.LoadServiceEvent;
import com.micdm.transportlive.events.events.LoadStationsEvent;
import com.micdm.transportlive.events.events.LoadVehiclesEvent;
import com.micdm.transportlive.events.events.RequestDonateEvent;
import com.micdm.transportlive.events.events.RequestLoadDonateProductsEvent;
import com.micdm.transportlive.events.events.RequestLoadForecastsEvent;
import com.micdm.transportlive.events.events.RequestLoadRoutesEvent;
import com.micdm.transportlive.events.events.RequestLoadServiceEvent;
import com.micdm.transportlive.events.events.RequestLoadStationsEvent;
import com.micdm.transportlive.events.events.RequestLoadVehiclesEvent;
import com.micdm.transportlive.events.events.RequestReconnectEvent;
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

public class IntentConverter {

    public Event convert(Intent intent) {
        switch (getEventTypeFromIntent(intent)) {
            case REQUEST_RECONNECT:
                return new RequestReconnectEvent();
            case REQUEST_LOAD_SERVICE:
                return new RequestLoadServiceEvent();
            case LOAD_SERVICE:
                return getLoadServiceEvent(intent);
            case REQUEST_LOAD_ROUTES:
                return new RequestLoadRoutesEvent();
            case LOAD_ROUTES:
                return getLoadRoutesEvent(intent);
            case REQUEST_SELECT_ROUTE:
                return getRequestSelectRouteEvent(intent);
            case REQUEST_UNSELECT_ROUTE:
                return getRequestUnselectRouteEvent(intent);
            case REQUEST_LOAD_VEHICLES:
                return new RequestLoadVehiclesEvent();
            case LOAD_VEHICLES:
                return getLoadVehiclesEvent(intent);
            case REQUEST_LOAD_STATIONS:
                return new RequestLoadStationsEvent();
            case LOAD_STATIONS:
                return getLoadStationsEvent(intent);
            case REQUEST_SELECT_STATION:
                return getRequestSelectStationEvent(intent);
            case REQUEST_UNSELECT_STATION:
                return getRequestUnselectStationEvent(intent);
            case REQUEST_LOAD_FORECASTS:
                return new RequestLoadForecastsEvent();
            case LOAD_FORECASTS:
                return getLoadForecastsEvent(intent);
            case REQUEST_LOAD_DONATE_PRODUCTS:
                return new RequestLoadDonateProductsEvent();
            case LOAD_DONATE_PRODUCTS:
                return getLoadDonateProductsEvent(intent);
            case REQUEST_DONATE:
                return getRequestDonateEvent(intent);
            case DONATE:
                return new DonateEvent();
            default:
                throw new RuntimeException("unknown event type");
        }
    }

    private EventType getEventTypeFromIntent(Intent intent) {
        String action = intent.getAction();
        String[] parts = action.split("\\.");
        String typeName = parts[parts.length - 1];
        for (EventType type: EventType.values()) {
            if (type.toString().equals(typeName)) {
                return type;
            }
        }
        throw new RuntimeException("unknown event type");
    }

    private LoadServiceEvent getLoadServiceEvent(Intent intent) {
        Service service = ((ServiceParcel) intent.getParcelableExtra("service")).getService();
        return new LoadServiceEvent(service);
    }

    private LoadRoutesEvent getLoadRoutesEvent(Intent intent) {
        List<SelectedRoute> routes = new ArrayList<SelectedRoute>();
        for (Parcelable parcel: intent.getParcelableArrayListExtra("routes")) {
            routes.add(((SelectedRouteParcel) parcel).getRoute());
        }
        return new LoadRoutesEvent(routes);
    }

    private RequestSelectRouteEvent getRequestSelectRouteEvent(Intent intent) {
        SelectedRoute route = ((SelectedRouteParcel) intent.getParcelableExtra("route")).getRoute();
        return new RequestSelectRouteEvent(route);
    }

    private RequestUnselectRouteEvent getRequestUnselectRouteEvent(Intent intent) {
        SelectedRoute route = ((SelectedRouteParcel) intent.getParcelableExtra("route")).getRoute();
        return new RequestUnselectRouteEvent(route);
    }

    private LoadVehiclesEvent getLoadVehiclesEvent(Intent intent) {
        int state = intent.getIntExtra("state", 0);
        List<Parcelable> parcels = intent.getParcelableArrayListExtra("vehicles");
        List<RoutePopulation> vehicles;
        if (parcels == null) {
            vehicles = null;
        } else {
            vehicles = new ArrayList<RoutePopulation>();
            for (Parcelable parcel: parcels) {
                vehicles.add(((RoutePopulationParcel) parcel).getPopulation());
            }
        }
        return new LoadVehiclesEvent(state, vehicles);
    }

    private LoadStationsEvent getLoadStationsEvent(Intent intent) {
        List<SelectedStation> stations = new ArrayList<SelectedStation>();
        for (Parcelable parcel: intent.getParcelableArrayListExtra("stations")) {
            stations.add(((SelectedStationParcel) parcel).getStation());
        }
        return new LoadStationsEvent(stations);
    }

    private RequestSelectStationEvent getRequestSelectStationEvent(Intent intent) {
        SelectedStation station = ((SelectedStationParcel) intent.getParcelableExtra("station")).getStation();
        return new RequestSelectStationEvent(station);
    }

    private RequestUnselectStationEvent getRequestUnselectStationEvent(Intent intent) {
        SelectedStation station = ((SelectedStationParcel) intent.getParcelableExtra("station")).getStation();
        return new RequestUnselectStationEvent(station);
    }

    private LoadForecastsEvent getLoadForecastsEvent(Intent intent) {
        int state = intent.getIntExtra("state", 0);
        List<Parcelable> parcels = intent.getParcelableArrayListExtra("forecasts");
        List<Forecast> forecasts;
        if (parcels == null) {
            forecasts = null;
        } else {
            forecasts = new ArrayList<Forecast>();
            for (Parcelable parcel: parcels) {
                forecasts.add(((ForecastParcel) parcel).getForecast());
            }
        }
        return new LoadForecastsEvent(state, forecasts);
    }

    private LoadDonateProductsEvent getLoadDonateProductsEvent(Intent intent) {
        List<DonateProduct> products = new ArrayList<DonateProduct>();
        for (Parcelable parcel: intent.getParcelableArrayListExtra("products")) {
            products.add(((DonateProductParcel) parcel).getProduct());
        }
        return new LoadDonateProductsEvent(products);
    }

    private RequestDonateEvent getRequestDonateEvent(Intent intent) {
        DonateProduct product = ((DonateProductParcel) intent.getParcelableExtra("product")).getProduct();
        return new RequestDonateEvent(product);
    }
}
