package com.micdm.transportlive.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.RouteInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.misc.ServiceHandler;

public class TransportRouteListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transport_route_list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setup();
    }

    private void setup() {
        Service service = ((ServiceHandler) getActivity()).getService();
        Transport transport = service.getTransportById(getArguments().getInt("id"));
        setupTitle(transport);
        setupRouteList(transport);
    }

    private void setupTitle(Transport transport) {
        TextView titleView = (TextView) getView().findViewById(R.id.title);
        titleView.setText(getTitle(transport.type));
    }

    private String getTitle(Transport.Type type) {
        switch (type) {
            case BUS:
                return getString(R.string.transport_type_bus);
            case TROLLEYBUS:
                return getString(R.string.transport_type_trolleybus);
            case TRAM:
                return getString(R.string.transport_type_tram);
            case TAXI:
                return getString(R.string.transport_type_taxi);
            default:
                throw new RuntimeException("unknown transport type");
        }
    }

    private void setupRouteList(Transport transport) {
        ViewGroup container = (ViewGroup) getView().findViewById(R.id.list);
        for (RouteInfo info: transport.getAllRouteInfo()) {
            View view = getRouteListItemView(info);
            container.addView(view);
        }
    }

    private View getRouteListItemView(final RouteInfo info) {
        View view = View.inflate(getActivity(), R.layout.view_route_list_item, null);
        final CheckBox checkbox = (CheckBox) view.findViewById(R.id.is_selected);
        checkbox.setChecked(info.route.isSelected);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                ((ServiceHandler) getActivity()).selectRoute(info, isChecked);
            }
        });
        TextView numberView = (TextView)view.findViewById(R.id.number);
        numberView.setText(String.valueOf(info.route.number));
        TextView startView = (TextView)view.findViewById(R.id.start);
        startView.setText(info.start);
        TextView finishView = (TextView)view.findViewById(R.id.finish);
        finishView.setText(info.finish);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkbox.toggle();
            }
        });
        return view;
    }
}
