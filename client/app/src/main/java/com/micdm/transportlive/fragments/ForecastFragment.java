package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.ForecastVehicle;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Station;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.interfaces.ForecastHandler;
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

        private final List<Group> groups = new ArrayList<Group>();

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
            return forecast.vehicles.size();
        }

        @Override
        public Group getGroup(int position) {
            return groups.get(position);
        }

        @Override
        public ForecastVehicle getChild(int groupPosition, int childPosition) {
            return getGroup(groupPosition).forecast.vehicles.get(childPosition);
        }

        @Override
        public long getGroupId(int position) {
            return getGroup(position).hashCode();
        }

        @Override
        public long getChildId(int groupPosition, int childPodition) {
            return getChild(groupPosition, childPodition).hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int position, boolean isExpanded, View view, ViewGroup viewGroup) {
            SelectedStationInfo info = getGroup(position).info;
            if (view == null) {
                view = View.inflate(getActivity(), R.layout.view_forecast_list_item_title, null);
            }
            ((TextView) view).setText(getString(R.string.fragment_forecast_station, Utils.getTransportName(getActivity(), info.transport), info.route.number,
                    info.direction.getStart(), info.direction.getFinish(), info.station.name));
            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isExpanded, View view, ViewGroup viewGroup) {
            // TODO: обработать ситуацию, когда транспорт не едет
            Forecast forecast = getGroup(groupPosition).forecast;
            ForecastVehicle vehicle = getChild(groupPosition, childPosition);
            if (view == null) {
                view = View.inflate(getActivity(), R.layout.view_forecast_list_item_vehicle_list_item, null);
            }
            TextView routeView = (TextView) view.findViewById(R.id.route);
            routeView.setText(getString(R.string.fragment_forecast_route, Utils.getTransportName(getActivity(), forecast.transport), vehicle.route.number));
            TextView arrivalTimeView = (TextView) view.findViewById(R.id.arrival_time);
            arrivalTimeView.setText(getArrivalTimeInMinutes(vehicle.arrivalTime));
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

    private ForecastHandler handler;
    private final ForecastHandler.OnLoadStationsListener onLoadStationsListener = new ForecastHandler.OnLoadStationsListener() {
        @Override
        public void onLoadStations(List<SelectedStationInfo> selected) {
            hideAllViews();
            showView(R.id.forecast_list);
            ForecastListAdapter adapter = new ForecastListAdapter();
            for (SelectedStationInfo info: selected) {
                adapter.addSelectedStation(info);
            }
            ExpandableListView view = getForecastListView();
            view.setAdapter(adapter);
            for (int i = 0; i < adapter.getGroupCount(); i += 1) {
                view.expandGroup(i);
            }
        }
    };
    private final ForecastHandler.OnSelectStationListener onSelectStationListener = new ForecastHandler.OnSelectStationListener() {
        @Override
        public void onSelectStation(SelectedStationInfo selected) {
            hideAllViews();
            showView(R.id.forecast_list);
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
            showView(R.id.no_station_selected);
        }
    };
    private final ForecastHandler.OnLoadForecastsListener onLoadForecastsListener = new ForecastHandler.OnLoadForecastsListener() {
        @Override
        public void onStart() {
            showView(R.id.loading);
        }
        @Override
        public void onFinish() {
            hideView(R.id.loading);
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
        handler = (ForecastHandler) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast_list, null);
        if (view != null) {
//            view.findViewById(R.id.station).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    handler.requestStationSelection();
//                }
//            });
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
        handler.addOnLoadStationsListener(onLoadStationsListener);
        handler.addOnSelectStationListener(onSelectStationListener);
        handler.addOnUnselectStationListener(onUnselectStationListener);
        handler.addOnUnselectAllStationsListener(onUnselectAllStationsListener);
        handler.addOnLoadForecastsListener(onLoadForecastsListener);
    }

    private void showView(int id) {
        getView().findViewById(id).setVisibility(View.VISIBLE);
    }

    private void hideView(int id) {
        getView().findViewById(id).setVisibility(View.GONE);
    }

    private void hideAllViews() {
        hideView(R.id.no_station_selected);
        hideView(R.id.forecast_list);
    }

    private ExpandableListView getForecastListView() {
        return ((ExpandableListView) getView().findViewById(R.id.forecast_list));
    }

    private ForecastListAdapter getForecastListAdapter() {
        return (ForecastListAdapter) getForecastListView().getExpandableListAdapter();
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeOnLoadStationsListener(onLoadStationsListener);
        handler.removeOnSelectStationListener(onSelectStationListener);
        handler.removeOnUnselectStationListener(onUnselectStationListener);
        handler.removeOnUnselectAllStationsListener(onUnselectAllStationsListener);
        handler.removeOnLoadForecastsListener(onLoadForecastsListener);
    }
}
