package com.micdm.transportlive.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.misc.ServiceHandler;

public class RouteListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route_list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setup();
    }

    private void setup() {
        Service service = ((ServiceHandler) getActivity()).getService();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        for (Transport transport: service.transports) {
            TransportRouteListFragment fragment = getTransportRouteListFragment(transport);
            transaction.add(R.id.route_list, fragment);
        }
        transaction.commit();
    }

    private TransportRouteListFragment getTransportRouteListFragment(Transport transport) {
        TransportRouteListFragment fragment = new TransportRouteListFragment();
        Bundle arguments = new Bundle();
        arguments.putInt("id", transport.id);
        fragment.setArguments(arguments);
        return fragment;
    }
}
