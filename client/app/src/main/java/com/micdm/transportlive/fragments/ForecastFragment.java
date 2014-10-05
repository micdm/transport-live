package com.micdm.transportlive.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.micdm.transportlive.App;
import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.ForecastVehicle;
import com.micdm.transportlive.data.SelectedStation;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Station;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.events.EventManager;
import com.micdm.transportlive.events.EventType;
import com.micdm.transportlive.events.events.LoadForecastsEvent;
import com.micdm.transportlive.events.events.LoadServiceEvent;
import com.micdm.transportlive.events.events.LoadStationsEvent;
import com.micdm.transportlive.events.events.RequestLoadForecastsEvent;
import com.micdm.transportlive.events.events.RequestLoadServiceEvent;
import com.micdm.transportlive.events.events.RequestLoadStationsEvent;
import com.micdm.transportlive.events.events.RequestUnselectStationEvent;
import com.micdm.transportlive.misc.RouteColors;
import com.micdm.transportlive.misc.Utils;
import com.micdm.transportlive.misc.analytics.Analytics;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ForecastFragment extends Fragment {

    private class ForecastListAdapter extends BaseExpandableListAdapter {

        private class GroupViewHolder {

            public final TextView stationView;
            public final TextView directionView;
            public final View removeView;

            private GroupViewHolder(TextView stationView, TextView directionView, View removeView) {
                this.stationView = stationView;
                this.directionView = directionView;
                this.removeView = removeView;
            }
        }

        private class ChildViewHolder {

            public final View noVehiclesView;
            public final View vehicleInfoView;
            public final View colorView;
            public final TextView routeView;
            public final View lowFloorView;
            public final TextView arrivalTimeView;

            private ChildViewHolder(View noVehiclesView, View vehicleInfoView, View colorView, TextView routeView, View lowFloorView, TextView arrivalTimeView) {
                this.noVehiclesView = noVehiclesView;
                this.vehicleInfoView = vehicleInfoView;
                this.colorView = colorView;
                this.routeView = routeView;
                this.lowFloorView = lowFloorView;
                this.arrivalTimeView = arrivalTimeView;
            }
        }

        private Service service;
        private RouteColors colors;
        private List<SelectedStation> selectedStations;
        private List<Forecast> forecasts;

        public void setService(Service service) {
            this.service = service;
            colors = new RouteColors(service);
        }

        public void setSelectedStations(List<SelectedStation> selectedStations) {
            this.selectedStations = selectedStations;
        }

        public void setForecasts(List<Forecast> forecasts) {
            this.forecasts = forecasts;
        }

        private Forecast getForecastForStation(SelectedStation selectedStation) {
            if (forecasts == null) {
                return null;
            }
            for (Forecast forecast: forecasts) {
                if (forecast.getTransportId() == selectedStation.getTransportId() && forecast.getStationId() == selectedStation.getStationId()) {
                    return forecast;
                }
            }
            return null;
        }

        @Override
        public int getGroupCount() {
            return (selectedStations == null) ? 0 : selectedStations.size();
        }

        @Override
        public int getChildrenCount(int position) {
            SelectedStation selectedStation = getGroup(position);
            Forecast forecast = getForecastForStation(selectedStation);
            if (forecast == null) {
                return 1;
            }
            int count = forecast.getVehicles().size();
            return (count == 0) ? 1 : count;
        }

        @Override
        public SelectedStation getGroup(int position) {
            return selectedStations.get(position);
        }

        @Override
        public ForecastVehicle getChild(int groupPosition, int childPosition) {
            SelectedStation selectedStation = getGroup(groupPosition);
            Forecast forecast = getForecastForStation(selectedStation);
            if (forecast == null) {
                return null;
            }
            List<ForecastVehicle> vehicles = forecast.getVehicles();
            return (vehicles.size() == 0) ? null : vehicles.get(childPosition);
        }

        @Override
        public long getGroupId(int position) {
            SelectedStation selectedStation = getGroup(position);
            return selectedStation.getTransportId() * 1000000 + selectedStation.getStationId();
        }

        @Override
        public long getChildId(int groupPosition, int childPodition) {
            ForecastVehicle vehicle = getChild(groupPosition, childPodition);
            // TODO: завести для каждой машины свой идентификатор
            return (vehicle == null) ? 0 : vehicle.hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int position, boolean isExpanded, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(getActivity(), R.layout.v__forecasts_list_item_title, null);
            }
            GroupViewHolder holder = getGroupViewHolder(view);
            final SelectedStation selectedStation = getGroup(position);
            Station station = service
                    .getTransportById(selectedStation.getTransportId())
                    .getStationById(selectedStation.getStationId());
            holder.stationView.setText(station.getName());
            Direction direction = service
                    .getTransportById(selectedStation.getTransportId())
                    .getRouteByNumber(selectedStation.getRouteNumber())
                    .getDirectionById(selectedStation.getDirectionId());
            holder.directionView.setText(getString(R.string.f__forecast__direction, direction.getFinish()));
            holder.removeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    App.get().getEventManager().publish(new RequestUnselectStationEvent(selectedStation));
                }
            });
            return view;
        }

        private GroupViewHolder getGroupViewHolder(View view) {
            GroupViewHolder holder = (GroupViewHolder) view.getTag();
            if (holder != null) {
                return holder;
            }
            TextView stationView = (TextView) view.findViewById(R.id.v__forecasts_list_item_title__station);
            TextView directionView = (TextView) view.findViewById(R.id.v__forecasts_list_item_title__direction);
            View removeView = view.findViewById(R.id.v__forecasts_list_item_title__remove_station);
            holder = new GroupViewHolder(stationView, directionView, removeView);
            view.setTag(holder);
            return holder;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isExpanded, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(getActivity(), R.layout.v__forecasts_list_item_vehicle_list_item, null);
            }
            ChildViewHolder holder = getChildViewHolder(view);
            SelectedStation selectedStation = getGroup(groupPosition);
            ForecastVehicle vehicle = getChild(groupPosition, childPosition);
            if (vehicle == null) {
                holder.noVehiclesView.setVisibility(View.VISIBLE);
                holder.vehicleInfoView.setVisibility(View.GONE);
            } else {
                holder.noVehiclesView.setVisibility(View.GONE);
                holder.vehicleInfoView.setVisibility(View.VISIBLE);
                holder.colorView.setBackgroundColor(colors.get(vehicle.getRouteNumber()));
                Transport transport = service.getTransportById(selectedStation.getTransportId());
                holder.routeView.setText(getString(R.string.f__forecast__route, Utils.getTransportName(getActivity(), transport), vehicle.getRouteNumber()));
                holder.lowFloorView.setVisibility(vehicle.isLowFloor() ? View.VISIBLE : View.GONE);
                holder.arrivalTimeView.setText(getArrivalTimeInMinutes(vehicle.getArrivalTime()));
            }
            return view;
        }

        private ChildViewHolder getChildViewHolder(View view) {
            ChildViewHolder holder = (ChildViewHolder) view.getTag();
            if (holder != null) {
                return holder;
            }
            View noVehiclesView = view.findViewById(R.id.v__forecasts_list_item_vehicle_list_item__no_vehicles);
            View vehicleInfoView = view.findViewById(R.id.v__forecasts_list_item_vehicle_list_item__vehicle_info);
            View colorView = view.findViewById(R.id.v__forecasts_list_item_vehicle_list_item__color);
            TextView routeView = (TextView) view.findViewById(R.id.v__forecasts_list_item_vehicle_list_item__route);
            View lowFloorView = view.findViewById(R.id.v__forecasts_list_item_vehicle_list_item__low_floor);
            TextView arrivalTimeView = (TextView) view.findViewById(R.id.v__forecasts_list_item_vehicle_list_item__arrival_time);
            holder = new ChildViewHolder(noVehiclesView, vehicleInfoView, colorView, routeView, lowFloorView, arrivalTimeView);
            view.setTag(holder);
            return holder;
        }

        private String getArrivalTimeInMinutes(int arrivalTime) {
            return getString(R.string.f__forecast__arrival_time, (int) Math.max(Math.ceil(arrivalTime / 60.0), 1));
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    private View noStationSelectedView;
    private ExpandableListView forecastsView;
    private View loadingView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f__forecasts, container, false);
        noStationSelectedView = view.findViewById(R.id.f__forecasts__no_station_selected);
        forecastsView = (ExpandableListView) view.findViewById(R.id.f__forecasts__forecast_list);
        forecastsView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        loadingView = view.findViewById(R.id.f__forecasts__loading);
        View selectStationView = view.findViewById(R.id.f__forecasts__select_station);
        selectStationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getChildFragmentManager();
                if (manager.findFragmentByTag(FragmentTag.SELECT_STATION) == null) {
                    (new SelectStationFragment()).show(manager, FragmentTag.SELECT_STATION);
                    App.get().getAnalytics().reportEvent(Analytics.Category.DIALOGS, Analytics.Action.SHOW, "select_station");
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        hideAllViews();
        subscribeForEvents();
        requestForData();
    }

    private void hideAllViews() {
        noStationSelectedView.setVisibility(View.GONE);
        forecastsView.setVisibility(View.GONE);
    }

    private void subscribeForEvents() {
        EventManager manager = App.get().getEventManager();
        manager.subscribe(this, EventType.LOAD_SERVICE, new EventManager.OnEventListener<LoadServiceEvent>() {
            @Override
            public void onEvent(LoadServiceEvent event) {
                ForecastListAdapter adapter = getAdapter();
                adapter.setService(event.getService());
                adapter.notifyDataSetChanged();
                expandAllGroups();
            }
        });
        manager.subscribe(this, EventType.LOAD_STATIONS, new EventManager.OnEventListener<LoadStationsEvent>() {
            @Override
            public void onEvent(LoadStationsEvent event) {
                hideAllViews();
                List<SelectedStation> selectedStations = event.getStations();
                if (selectedStations.size() == 0) {
                    noStationSelectedView.setVisibility(View.VISIBLE);
                } else {
                    ForecastListAdapter adapter = getAdapter();
                    adapter.setSelectedStations(selectedStations);
                    adapter.notifyDataSetChanged();
                    expandAllGroups();
                    forecastsView.setVisibility(View.VISIBLE);
                }
            }
        });
        manager.subscribe(this, EventType.LOAD_FORECASTS, new EventManager.OnEventListener<LoadForecastsEvent>() {
            @Override
            public void onEvent(LoadForecastsEvent event) {
                switch (event.getState()) {
                    case LoadForecastsEvent.STATE_START:
                        loadingView.setVisibility(View.VISIBLE);
                        break;
                    case LoadForecastsEvent.STATE_FINISH:
                        loadingView.setVisibility(View.GONE);
                        break;
                    case LoadForecastsEvent.STATE_COMPLETE:
                        List<Forecast> forecasts = event.getForecasts();
                        for (Forecast forecast: forecasts) {
                            Collections.sort(forecast.getVehicles(), new Comparator<ForecastVehicle>() {
                                @Override
                                public int compare(ForecastVehicle a, ForecastVehicle b) {
                                    return (a.getArrivalTime() > b.getArrivalTime()) ? 1 : -1;
                                }
                            });
                        }
                        ForecastListAdapter adapter = getAdapter();
                        adapter.setForecasts(forecasts);
                        adapter.notifyDataSetChanged();
                        expandAllGroups();
                        break;
                }
            }
        });
    }

    private void requestForData() {
        EventManager manager = App.get().getEventManager();
        manager.publish(new RequestLoadServiceEvent());
        manager.publish(new RequestLoadStationsEvent());
        manager.publish(new RequestLoadForecastsEvent());
    }

    private ForecastListAdapter getAdapter() {
        ForecastListAdapter adapter = (ForecastListAdapter) forecastsView.getExpandableListAdapter();
        if (adapter == null) {
            adapter = new ForecastListAdapter();
            forecastsView.setAdapter(adapter);
        }
        return adapter;
    }

    private void expandAllGroups() {
        ForecastListAdapter adapter = getAdapter();
        for (int i = 0; i < adapter.getGroupCount(); i += 1) {
            forecastsView.expandGroup(i);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        App.get().getEventManager().unsubscribeAll(this);
    }
}
