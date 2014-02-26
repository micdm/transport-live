package com.micdm.transportlive.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.misc.ServiceCache;
import com.micdm.transportlive.misc.ServiceLoader;

public abstract class ServiceFragment extends Fragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadService();
    }

    private void loadService() {
        Service service = ServiceCache.get(getActivity());
        if (service == null) {
            loadTransports(new Service());
        } else {
            onServiceReady(service);
        }
    }

    private void loadTransports(Service service) {
        showLoadingMessage(getString(R.string.first_loading_transports));
        ServiceLoader.loadTransports(getActivity(), service, new ServiceLoader.OnLoadListener() {
            @Override
            public void onLoad(Service service) {
                hideLoadingMessage();
                loadRoutes(service);
            }
            @Override
            public void onNoConnection() {

            }
        });
    }

    private void loadRoutes(Service service) {
        showLoadingMessage(getString(R.string.first_loading_routes));
        ServiceLoader.loadRoutes(getActivity(), service, new ServiceLoader.OnLoadListener() {
            @Override
            public void onLoad(Service service) {
                hideLoadingMessage();
                loadStations(service);
            }
            @Override
            public void onNoConnection() {

            }
        });
    }

    private void loadStations(Service service) {
        showLoadingMessage(getString(R.string.first_loading_stations));
        ServiceLoader.loadStations(getActivity(), service, new ServiceLoader.OnLoadListener() {
            @Override
            public void onLoad(Service service) {
                hideLoadingMessage();
                onServiceReady(service);
            }
            @Override
            public void onNoConnection() {

            }
        });
    }

    private void showLoadingMessage(String message) {
        ProgressFragment fragment = new ProgressFragment();
        Bundle arguments = new Bundle();
        arguments.putString("message", message);
        fragment.setArguments(arguments);
        fragment.show(getActivity().getSupportFragmentManager(), "progress");
    }

    private void hideLoadingMessage() {
        ProgressFragment fragment = (ProgressFragment) getActivity().getSupportFragmentManager().findFragmentByTag("progress");
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    protected void onServiceReady(Service service) {}
}
