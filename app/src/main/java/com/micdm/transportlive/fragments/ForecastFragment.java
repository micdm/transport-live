package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.misc.ForecastHandler;
import com.micdm.transportlive.misc.Utils;

public class ForecastFragment extends Fragment {

    private ForecastHandler handler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        handler = (ForecastHandler) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast, null);
        view.findViewById(R.id.select_station).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.requestStationSelection();
            }
        });
        view.findViewById(R.id.reselect_station).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.requestStationSelection();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        hideAllViews();
        handler.setOnSelectStationListener(new ForecastHandler.OnSelectStationListener() {
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
        });
        handler.setOnLoadForecastListener(new ForecastHandler.OnLoadForecastListener() {
            @Override
            public void onLoadForecast() {
                hideAllViews();
                showView(R.id.forecast);
            }
        });
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.setOnSelectStationListener(null);
        handler.setOnLoadForecastListener(null);
    }
}
