package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.ForecastVehicle;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Station;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.interfaces.ForecastHandler;
import com.micdm.transportlive.interfaces.ServiceHandler;
import com.micdm.transportlive.misc.RouteColors;
import com.micdm.transportlive.misc.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ForecastFragment extends Fragment {

    private class ForecastListAdapter extends BaseExpandableListAdapter {

        private class Group {

            public final SelectedStationInfo info;
            public Forecast forecast;

            public Group(SelectedStationInfo info) {
                this.info = info;
            }
        }

        private final RouteColors colors;
        private final List<Group> groups = new ArrayList<Group>();

        public ForecastListAdapter(Service service) {
            this.colors = new RouteColors(service);
        }

        @Override
        public int getGroupCount() {
            return groups.size();
        }

        @Override
        public int getChildrenCount(int position) {
            Forecast forecast = getGroup(position).forecast;
            if (forecast == null) {
                return 0;
            }
            int count = forecast.vehicles.size();
            return count == 0 ? 1 : count;
        }

        @Override
        public Group getGroup(int position) {
            return groups.get(position);
        }

        @Override
        public ForecastVehicle getChild(int groupPosition, int childPosition) {
            List<ForecastVehicle> vehicles = getGroup(groupPosition).forecast.vehicles;
            return vehicles.size() == 0 ? null : vehicles.get(childPosition);
        }

        @Override
        public long getGroupId(int position) {
            return getGroup(position).hashCode();
        }

        @Override
        public long getChildId(int groupPosition, int childPodition) {
            ForecastVehicle vehicle = getChild(groupPosition, childPodition);
            return vehicle == null ? 0 : vehicle.hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int position, boolean isExpanded, View view, ViewGroup viewGroup) {
            final SelectedStationInfo info = getGroup(position).info;
            if (view == null) {
                view = View.inflate(getActivity(), R.layout.v__forecasts_list_item_title, null);
            }
            TextView stationView = (TextView) view.findViewById(R.id.v__forecasts_list_item_title__station);
            stationView.setText(getString(R.string.fragment_forecast_station, info.direction.getStart(), info.direction.getFinish(), info.station.name));
            ImageButton removeView = (ImageButton) view.findViewById(R.id.v__forecasts_list_item_title__remove_station);
            removeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    forecastHandler.unselectStation(info.transport, info.station);
                }
            });
            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isExpanded, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(getActivity(), R.layout.v__forecasts_list_item_vehicle_list_item, null);
            }
            Forecast forecast = getGroup(groupPosition).forecast;
            View noVehiclesView = view.findViewById(R.id.v__forecasts_list_item_vehicle_list_item__no_vehicles);
            View vehicleInfoView = view.findViewById(R.id.v__forecasts_list_item_vehicle_list_item__vehicle_info);
            if (forecast.vehicles.size() == 0) {
                noVehiclesView.setVisibility(View.VISIBLE);
                vehicleInfoView.setVisibility(View.GONE);
            } else {
                noVehiclesView.setVisibility(View.GONE);
                vehicleInfoView.setVisibility(View.VISIBLE);
                ForecastVehicle vehicle = getChild(groupPosition, childPosition);
                View colorView = view.findViewById(R.id.v__forecasts_list_item_vehicle_list_item__color);
                colorView.setBackgroundColor(colors.get(vehicle.route));
                TextView routeView = (TextView) view.findViewById(R.id.v__forecasts_list_item_vehicle_list_item__route);
                routeView.setText(getString(R.string.fragment_forecast_route, Utils.getTransportName(getActivity(), forecast.transport), vehicle.route.number));
                View lowFloorView = view.findViewById(R.id.v__forecasts_list_item_vehicle_list_item__low_floor);
                lowFloorView.setVisibility(vehicle.isLowFloor ? View.VISIBLE : View.GONE);
                TextView arrivalTimeView = (TextView) view.findViewById(R.id.v__forecasts_list_item_vehicle_list_item__arrival_time);
                arrivalTimeView.setText(getArrivalTimeInMinutes(vehicle.arrivalTime));
            }
            return view;
        }

        private String getArrivalTimeInMinutes(int arrivalTime) {
            return getString(R.string.fragment_forecast_arrival_time, (int) Math.max(Math.ceil(arrivalTime / 60.0), 1));
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        public int addSelectedStation(SelectedStationInfo selected) {
            groups.add(new Group(selected));
            return groups.size() - 1;
        }

        public void removeSelectedStation(Transport transport, Station station) {
            Iterator<Group> iterator = groups.iterator();
            while (iterator.hasNext()) {
                SelectedStationInfo info = iterator.next().info;
                if (info.transport.equals(transport) && info.station.equals(station)) {
                    iterator.remove();
                }
            }
        }

        public void updateForecast(Forecast forecast) {
            Group group = getGroupForStation(forecast.transport, forecast.station);
            group.forecast = forecast;
        }

        private Group getGroupForStation(Transport transport, Station station) {
            for (Group group: groups) {
                if (group.info.transport.equals(transport) && group.info.station.equals(station)) {
                    return group;
                }
            }
            throw new RuntimeException("cannot find group for station");
        }
    }

    private ServiceHandler serviceHandler;
    private final ServiceHandler.OnLoadServiceListener onLoadServiceListener = new ServiceHandler.OnLoadServiceListener() {
        @Override
        public void onLoadService(Service service) {
            getForecastListView().setAdapter(new ForecastListAdapter(service));
        }
    };

    private ForecastHandler forecastHandler;
    private final ForecastHandler.OnLoadStationsListener onLoadStationsListener = new ForecastHandler.OnLoadStationsListener() {
        @Override
        public void onLoadStations(List<SelectedStationInfo> selected) {
            hideAllViews();
            showView(R.id.f__forecasts__forecast_list);
            ExpandableListView view = getForecastListView();
            ForecastListAdapter adapter = getForecastListAdapter();
            for (SelectedStationInfo info: selected) {
                adapter.addSelectedStation(info);
            }
            for (int i = 0; i < adapter.getGroupCount(); i += 1) {
                view.expandGroup(i);
            }
        }
    };
    private final ForecastHandler.OnSelectStationListener onSelectStationListener = new ForecastHandler.OnSelectStationListener() {
        @Override
        public void onSelectStation(SelectedStationInfo selected) {
            hideAllViews();
            showView(R.id.f__forecasts__forecast_list);
            ForecastListAdapter adapter = getForecastListAdapter();
            int position = adapter.addSelectedStation(selected);
            adapter.notifyDataSetChanged();
            getForecastListView().expandGroup(position);
        }
    };
    private final ForecastHandler.OnUnselectStationListener onUnselectStationListener = new ForecastHandler.OnUnselectStationListener() {
        @Override
        public void onUnselectStation(Transport transport, Station station) {
            ForecastListAdapter adapter = getForecastListAdapter();
            adapter.removeSelectedStation(transport, station);
            adapter.notifyDataSetChanged();
        }
    };
    private final ForecastHandler.OnUnselectAllStationsListener onUnselectAllStationsListener = new ForecastHandler.OnUnselectAllStationsListener() {
        @Override
        public void onUnselectAllStations() {
            hideAllViews();
            showView(R.id.f__forecasts__no_station_selected);
        }
    };
    private final ForecastHandler.OnLoadForecastsListener onLoadForecastsListener = new ForecastHandler.OnLoadForecastsListener() {
        @Override
        public void onStart() {
            showView(R.id.f__forecasts__loading);
        }
        @Override
        public void onFinish() {
            hideView(R.id.f__forecasts__loading);
        }
        @Override
        public void onLoadForecasts(List<Forecast> forecasts) {
            ForecastListAdapter adapter = getForecastListAdapter();
            for (Forecast forecast: forecasts) {
                Collections.sort(forecast.vehicles, new Comparator<ForecastVehicle>() {
                    @Override
                    public int compare(ForecastVehicle a, ForecastVehicle b) {
                        return a.arrivalTime > b.arrivalTime ? 1 : -1;
                    }
                });
                adapter.updateForecast(forecast);
            }
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        serviceHandler = (ServiceHandler) getActivity();
        forecastHandler = (ForecastHandler) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f__forecasts, null);
        if (view != null) {
            view.findViewById(R.id.f__forecasts__select_station).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    forecastHandler.requestStationSelection();
                }
            });
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        hideAllViews();
        serviceHandler.addOnLoadServiceListener(onLoadServiceListener);
        forecastHandler.addOnLoadStationsListener(onLoadStationsListener);
        forecastHandler.addOnSelectStationListener(onSelectStationListener);
        forecastHandler.addOnUnselectStationListener(onUnselectStationListener);
        forecastHandler.addOnUnselectAllStationsListener(onUnselectAllStationsListener);
        forecastHandler.addOnLoadForecastsListener(onLoadForecastsListener);
    }

    private void showView(int id) {
        getView().findViewById(id).setVisibility(View.VISIBLE);
    }

    private void hideView(int id) {
        getView().findViewById(id).setVisibility(View.GONE);
    }

    private void hideAllViews() {
        hideView(R.id.f__forecasts__no_station_selected);
        hideView(R.id.f__forecasts__forecast_list);
    }

    private ExpandableListView getForecastListView() {
        return ((ExpandableListView) getView().findViewById(R.id.f__forecasts__forecast_list));
    }

    private ForecastListAdapter getForecastListAdapter() {
        return (ForecastListAdapter) getForecastListView().getExpandableListAdapter();
    }

    @Override
    public void onStop() {
        super.onStop();
        serviceHandler.removeOnLoadServiceListener(onLoadServiceListener);
        forecastHandler.removeOnLoadStationsListener(onLoadStationsListener);
        forecastHandler.removeOnSelectStationListener(onSelectStationListener);
        forecastHandler.removeOnUnselectStationListener(onUnselectStationListener);
        forecastHandler.removeOnUnselectAllStationsListener(onUnselectAllStationsListener);
        forecastHandler.removeOnLoadForecastsListener(onLoadForecastsListener);
    }
}
