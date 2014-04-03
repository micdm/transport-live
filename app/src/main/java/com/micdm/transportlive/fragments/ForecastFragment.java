package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Station;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.misc.ServiceHandler;
import com.micdm.transportlive.misc.Utils;

import java.util.List;

public class ForecastFragment extends Fragment {

    private abstract class ListAdapter<ItemType> extends BaseAdapter {

        private List<ItemType> items;

        public ListAdapter(List<ItemType> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ItemType getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(getActivity(), R.layout.view_forecast_list_item, null);
            }
            ((TextView) view).setText(getItemName(position));
            return view;
        }

        protected abstract String getItemName(int position);

    }

    private class TransportListAdapter extends ListAdapter<Transport> {

        public TransportListAdapter(List<Transport> items) {
            super(items);
        }

        @Override
        protected String getItemName(int position) {
            return Utils.getTransportName(getActivity(), getItem(position));
        }
    }

    private class RouteListAdapter extends ListAdapter<Route> {

        public RouteListAdapter(List<Route> items) {
            super(items);
        }

        @Override
        protected String getItemName(int position) {
            return String.format("№ %s", getItem(position).number);
        }
    }

    private class DirectionListAdapter extends ListAdapter<Direction> {

        public DirectionListAdapter(List<Direction> items) {
            super(items);
        }

        @Override
        protected String getItemName(int position) {
            Direction direction = getItem(position);
            return String.format("%s → %s", direction.getStart().name, direction.getFinish().name);
        }
    }

    private class StationListAdapter extends ListAdapter<Station> {

        public StationListAdapter(List<Station> items) {
            super(items);
        }

        @Override
        protected String getItemName(int position) {
            return getItem(position).name;
        }
    }

    private ServiceHandler handler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        handler = (ServiceHandler) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast, null);
        Spinner transportListView = ((Spinner) view.findViewById(R.id.transport_list));
        final Spinner routeListView = ((Spinner) view.findViewById(R.id.route_list));
        transportListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Transport transport = ((TransportListAdapter) parent.getAdapter()).getItem(position);
                routeListView.setAdapter(new RouteListAdapter(transport.routes));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        final Spinner directionListView = ((Spinner) view.findViewById(R.id.direction_list));
        routeListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Route route = ((RouteListAdapter) parent.getAdapter()).getItem(position);
                directionListView.setAdapter(new DirectionListAdapter(route.directions));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        final Spinner stationListView = ((Spinner) view.findViewById(R.id.station_list));
        directionListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Direction direction = ((DirectionListAdapter) parent.getAdapter()).getItem(position);
                stationListView.setAdapter(new StationListAdapter(direction.stations));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handler.setOnLoadServiceListener(new ServiceHandler.OnLoadServiceListener() {
            @Override
            public void onLoadService(Service service) {
                Spinner view = (Spinner) getView().findViewById(R.id.transport_list);
                view.setAdapter(new TransportListAdapter(service.transports));
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.setOnLoadServiceListener(null);
    }
}
