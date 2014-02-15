package com.micdm.transportlive.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.micdm.transportlive.MainActivity;
import com.micdm.transportlive.R;
import com.micdm.transportlive.data.RouteInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;

public class TransportRouteListFragment extends Fragment {

    public static interface OnRouteCheckChangeListener {
        public void onRouteCheckChange(int transportId, int routeNumber, boolean isChecked);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transport_route_list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Transport transport = getTransport();
        setupTitle(transport);
        setupRouteList(transport);
    }

    private Transport getTransport() {
        int id = getArguments().getInt("id");
        Service service = ((MainActivity) getActivity()).getService();
        return service.getTransportById(id);
    }

    private void setupTitle(Transport transport) {
        TextView titleView = (TextView) getView().findViewById(R.id.title);
        switch (transport.type) {
            case BUS:
                titleView.setText(getString(R.string.transport_type_bus));
                break;
            case TROLLEYBUS:
                titleView.setText(getString(R.string.transport_type_trolleybus));
                break;
            case TRAM:
                titleView.setText(getString(R.string.transport_type_tram));
                break;
            case TAXI:
                titleView.setText(getString(R.string.transport_type_taxi));
                break;
        }
    }

    private void setupRouteList(Transport transport) {
        ViewGroup container = (ViewGroup)getView().findViewById(R.id.list);
        for (RouteInfo info: transport.getAllRouteInfo()) {
            addRouteListItem(container, transport, info);
        }
    }

    private void addRouteListItem(ViewGroup container, final Transport transport, final RouteInfo info) {
        View view = View.inflate(getActivity(), R.layout.view_route_list_item, null);
        CheckBox checkbox = (CheckBox)view.findViewById(R.id.is_checked);
        checkbox.setChecked(info.route.isChecked);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                ((OnRouteCheckChangeListener) getActivity()).onRouteCheckChange(transport.id, info.route.number, isChecked);
            }
        });
        TextView numberView = (TextView)view.findViewById(R.id.number);
        numberView.setText(String.valueOf(info.route.number));
        TextView startView = (TextView)view.findViewById(R.id.start);
        startView.setText(info.start);
        TextView finishView = (TextView)view.findViewById(R.id.finish);
        finishView.setText(info.finish);
        container.addView(view);
    }
}
