package com.micdm.transportlive.fragments;

import android.app.Activity;
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

    private ServiceHandler handler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        handler = (ServiceHandler) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route_list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handler.setOnLoadServiceListener(new ServiceHandler.OnLoadServiceListener() {
            @Override
            public void onLoadService(Service service) {
                setup(service);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.setOnLoadServiceListener(null);
    }

    private void setup(Service service) {
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
