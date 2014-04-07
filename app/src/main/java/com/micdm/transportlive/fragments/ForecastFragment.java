package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.ForecastVehicle;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.interfaces.ForecastHandler;
import com.micdm.transportlive.misc.Utils;

import java.util.Collections;
import java.util.Comparator;

public class ForecastFragment extends Fragment {

    private ForecastHandler handler;
    private ForecastHandler.OnSelectStationListener onSelectStationListener = new ForecastHandler.OnSelectStationListener() {
        @Override
        public void onSelectStation(SelectedStationInfo selected) {
            hideAllViews();
            if (selected == null) {
                showView(R.id.no_station_selected);
            } else {
                showView(R.id.forecast);
                TextView stationView = ((TextView) getView().findViewById(R.id.station));
                stationView.setText(getString(R.string.fragment_forecast_station, Utils.getTransportName(getActivity(), selected.transport),
                        selected.route.number, selected.direction.getStart(), selected.direction.getFinish(), selected.station.name));
            }
        }
    };
    private ForecastHandler.OnLoadForecastListener onLoadForecastListener = new ForecastHandler.OnLoadForecastListener() {
        @Override
        public void onStart() {
            showView(R.id.loading);
        }
        @Override
        public void onFinish() {
            hideView(R.id.loading);
        }
        @Override
        public void onLoadForecast(Forecast forecast) {
            update(forecast);
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        handler = (ForecastHandler) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast, null);
        if (view != null) {
            view.findViewById(R.id.select_station).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handler.requestStationSelection();
                }
            });
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        hideAllViews();
        handler.addOnSelectStationListener(onSelectStationListener);
        handler.addOnLoadForecastListener(onLoadForecastListener);
    }

    private void showView(int id) {
        getView().findViewById(id).setVisibility(View.VISIBLE);
    }

    private void hideView(int id) {
        getView().findViewById(id).setVisibility(View.GONE);
    }

    private void hideAllViews() {
        hideView(R.id.no_station_selected);
        hideView(R.id.forecast);
        hideView(R.id.vehicle_container);
        hideView(R.id.no_vehicles);
    }

    private void update(Forecast forecast) {
        hideAllViews();
        showView(R.id.forecast);
        if (forecast.vehicles.isEmpty()) {
            showView(R.id.no_vehicles);
            return;
        }
        Collections.sort(forecast.vehicles, new Comparator<ForecastVehicle>() {
            @Override
            public int compare(ForecastVehicle a, ForecastVehicle b) {
                return a.arrivalTime > b.arrivalTime ? 1 : -1;
            }
        });
        showView(R.id.vehicle_container);
        setupVehicleList(forecast);
    }

    private void setupVehicleList(Forecast forecast) {
        ViewGroup containerView = (ViewGroup) getView().findViewById(R.id.vehicle_list);
        containerView.removeAllViews();
        for (ForecastVehicle vehicle: forecast.vehicles) {
            View vehicleView = getVehicleView(vehicle);
            containerView.addView(vehicleView);
        }
    }

    private View getVehicleView(ForecastVehicle vehicle) {
        Context context = getActivity();
        View view = View.inflate(context, R.layout.view_forecast_vehicle_list_item, null);
        TextView routeView = (TextView) view.findViewById(R.id.route);
        routeView.setText(getString(R.string.fragment_forecast_route, Utils.getTransportName(context, vehicle.transport), vehicle.route.number));
        TextView arrivalTimeView = (TextView) view.findViewById(R.id.arrival_time);
        arrivalTimeView.setText(getArrivalTimeInMinutes(vehicle.arrivalTime));
        return view;
    }

    private String getArrivalTimeInMinutes(int arrivalTime) {
        return getString(R.string.fragment_forecast_arrival_time, (int) Math.max(Math.ceil(arrivalTime / 60.0), 1));
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeOnSelectStationListener(onSelectStationListener);
        handler.removeOnLoadForecastListener(onLoadForecastListener);
    }
}
