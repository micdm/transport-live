package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Station;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.interfaces.ForecastHandler;
import com.micdm.transportlive.interfaces.ServiceHandler;
import com.micdm.transportlive.misc.Utils;

import java.util.List;

public class SelectStationFragment extends DialogFragment {

    private abstract class ListAdapter<ItemType> extends BaseAdapter {

        private final List<ItemType> items;

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
                view = View.inflate(getActivity(), R.layout.v__select_station_list_item, null);
            }
            ((TextView) view).setText(getItemName(position));
            return view;
        }

        protected abstract String getItemName(int position);

        public int getItemPosition(ItemType item) {
            return items.indexOf(item);
        }
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
            return getString(R.string.f__select_station__route_list_item, getItem(position).number);
        }
    }

    private class DirectionListAdapter extends ListAdapter<Direction> {

        public DirectionListAdapter(List<Direction> items) {
            super(items);
        }

        @Override
        protected String getItemName(int position) {
            Direction direction = getItem(position);
            return getString(R.string.f__select_station__direction_list_item, direction.getStart(), direction.getFinish());
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

    private ServiceHandler serviceHandler;
    private final ServiceHandler.OnLoadServiceListener onLoadServiceListener = new ServiceHandler.OnLoadServiceListener() {
        @Override
        public void onLoadService(Service service) {
            Transport transport = service.transports.get(0);
            Route route = transport.routes.get(0);
            Direction direction = route.directions.get(0);
            Station station = direction.stations.get(0);
            setup(service, transport, route, direction, station);
        }
    };

    private ForecastHandler forecastHandler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        serviceHandler = (ServiceHandler) activity;
        forecastHandler = (ForecastHandler) activity;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.f__select_station__title);
        builder.setView(View.inflate(getActivity(), R.layout.f__select_station, null));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Transport transport = (Transport) getTransportListSpinner().getSelectedItem();
                Route route = (Route) getRouteListSpinner().getSelectedItem();
                Direction direction = (Direction) getDirectionListSpinner().getSelectedItem();
                Station station = (Station) getStationListSpinner().getSelectedItem();
                forecastHandler.selectStation(new SelectedStationInfo(transport, route, direction, station));
            }
        });
        return builder.create();
    }

    private Spinner getTransportListSpinner() {
        return (Spinner) getDialog().findViewById(R.id.f__select_station__transport_list);
    }

    private Spinner getRouteListSpinner() {
        return (Spinner) getDialog().findViewById(R.id.f__select_station__route_list);
    }

    private Spinner getDirectionListSpinner() {
        return (Spinner) getDialog().findViewById(R.id.f__select_station__direction_list);
    }

    private Spinner getStationListSpinner() {
        return (Spinner) getDialog().findViewById(R.id.f__select_station__station_list);
    }

    @Override
    public void onStart() {
        super.onStart();
        serviceHandler.addOnLoadServiceListener(onLoadServiceListener);
    }

    private void setup(Service service, Transport transport, Route route, Direction direction, Station station) {
        setupTransportListSpinner(service.transports, transport);
        setupRouteListSpinner(transport.routes, route);
        setupDirectionListSpinner(route.directions, direction);
        setupStationListSpinner(direction.stations, station);
    }

    private void setupTransportListSpinner(List<Transport> transports, Transport transport) {
        Spinner view = getTransportListSpinner();
        TransportListAdapter adapter = new TransportListAdapter(transports);
        view.setAdapter(adapter);
        int position = adapter.getItemPosition(transport);
        view.setSelection(position, false);
        view.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Transport transport = (Transport) parent.getAdapter().getItem(position);
                getRouteListSpinner().setAdapter(new RouteListAdapter(transport.routes));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRouteListSpinner(List<Route> routes, Route route) {
        Spinner view = getRouteListSpinner();
        RouteListAdapter adapter = new RouteListAdapter(routes);
        view.setAdapter(adapter);
        int position = adapter.getItemPosition(route);
        view.setSelection(position, false);
        view.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Route route = (Route) parent.getAdapter().getItem(position);
                getDirectionListSpinner().setAdapter(new DirectionListAdapter(route.directions));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupDirectionListSpinner(List<Direction> directions, Direction direction) {
        Spinner view = getDirectionListSpinner();
        DirectionListAdapter adapter = new DirectionListAdapter(directions);
        view.setAdapter(adapter);
        int position = adapter.getItemPosition(direction);
        view.setSelection(position, false);
        view.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Direction direction = (Direction) parent.getAdapter().getItem(position);
                getStationListSpinner().setAdapter(new StationListAdapter(direction.stations));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupStationListSpinner(List<Station> stations, Station station) {
        Spinner view = getStationListSpinner();
        StationListAdapter adapter = new StationListAdapter(stations);
        view.setAdapter(adapter);
        int position = adapter.getItemPosition(station);
        view.setSelection(position, false);
    }

    @Override
    public void onStop() {
        super.onStop();
        serviceHandler.removeOnLoadServiceListener(onLoadServiceListener);
    }
}
