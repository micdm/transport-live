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
import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.ForecastVehicle;
import com.micdm.transportlive.data.SelectedStation;
import com.micdm.transportlive.data.service.Direction;
import com.micdm.transportlive.data.service.Route;
import com.micdm.transportlive.data.service.Service;
import com.micdm.transportlive.data.service.Station;
import com.micdm.transportlive.data.service.Transport;
import com.micdm.transportlive.events.EventManager;
import com.micdm.transportlive.events.EventType;
import com.micdm.transportlive.events.events.LoadServiceEvent;
import com.micdm.transportlive.events.events.LoadStationsEvent;
import com.micdm.transportlive.events.events.RemoveAllDataEvent;
import com.micdm.transportlive.events.events.RequestFocusVehicleEvent;
import com.micdm.transportlive.events.events.RequestLoadServiceEvent;
import com.micdm.transportlive.events.events.RequestLoadStationsEvent;
import com.micdm.transportlive.events.events.RequestUnselectStationEvent;
import com.micdm.transportlive.events.events.UpdateForecastEvent;
import com.micdm.transportlive.misc.RouteColors;
import com.micdm.transportlive.misc.Utils;
import com.micdm.transportlive.misc.analytics.Analytics;

import java.util.ArrayList;
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

        private final Comparator<ForecastVehicle> VEHICLE_COMPARATOR = new Comparator<ForecastVehicle>() {
            @Override
            public int compare(ForecastVehicle a, ForecastVehicle b) {
                return (a.getArrivalTime() > b.getArrivalTime()) ? 1 : -1;
            }
        };

        private Service service;
        private RouteColors colors;

        private List<SelectedStation> selectedStations;
        private final List<Forecast> forecasts = new ArrayList<Forecast>();

        public void setService(Service service) {
            this.service = service;
            colors = new RouteColors(service);
        }

        public void setSelectedStations(List<SelectedStation> selectedStations) {
            this.selectedStations = selectedStations;
        }

        public void updateForecast(Forecast forecast) {
            removeForecast(forecast.getTransportId(), forecast.getStationId());
            Collections.sort(forecast.getVehicles(), VEHICLE_COMPARATOR);
            forecasts.add(forecast);
        }

        private void removeForecast(int transportId, int stationId) {
            Forecast forecast = getForecast(transportId, stationId);
            if (forecast != null) {
                forecasts.remove(forecast);
            }
        }

        public void removeAllForecasts() {
            forecasts.clear();
        }

        private Forecast getForecast(int transportId, int stationId) {
            for (Forecast forecast: forecasts) {
                if (forecast.getTransportId() == transportId && forecast.getStationId() == stationId) {
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
            Forecast forecast = getForecast(selectedStation.getTransportId(), selectedStation.getStationId());
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
            Forecast forecast = getForecast(selectedStation.getTransportId(), selectedStation.getStationId());
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
            return 0;
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
                Transport transport = service.getTransportById(selectedStation.getTransportId());
                Route route = transport.getRouteByNumber(vehicle.getRouteNumber());
                holder.noVehiclesView.setVisibility(View.GONE);
                holder.vehicleInfoView.setVisibility(View.VISIBLE);
                holder.colorView.setBackgroundColor(colors.get(route));
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
            return getChild(groupPosition, childPosition) != null;
        }
    }

    private View noStationSelectedView;
    private ExpandableListView forecastsView;

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
        forecastsView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ForecastListAdapter adapter = ((ForecastListAdapter) parent.getExpandableListAdapter());
                SelectedStation station = adapter.getGroup(groupPosition);
                ForecastVehicle vehicle = adapter.getChild(groupPosition, childPosition);
                if (vehicle != null) {
                    App.get().getEventManager().publish(new RequestFocusVehicleEvent(vehicle.getNumber(), station.getTransportId(), vehicle.getRouteNumber()));
                    return true;
                }
                return false;
            }
        });
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
        manager.subscribe(this, EventType.UPDATE_FORECAST, new EventManager.OnEventListener<UpdateForecastEvent>() {
            @Override
            public void onEvent(UpdateForecastEvent event) {
                ForecastListAdapter adapter = getAdapter();
                adapter.updateForecast(event.getForecast());
                adapter.notifyDataSetChanged();
            }
        });
        manager.subscribe(this, EventType.REMOVE_ALL_DATA, new EventManager.OnEventListener<RemoveAllDataEvent>() {
            @Override
            public void onEvent(RemoveAllDataEvent event) {
                ForecastListAdapter adapter = getAdapter();
                adapter.removeAllForecasts();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void requestForData() {
        EventManager manager = App.get().getEventManager();
        manager.publish(new RequestLoadServiceEvent());
        manager.publish(new RequestLoadStationsEvent());
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
