package com.micdm.transportlive.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.misc.ServiceCache;
import com.micdm.transportlive.misc.ServiceLoader;

/**
 * TODO: добавить пустые конструкторы для фрагментов
 */
public class RouteListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route_list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ServiceCache cache = ServiceCache.getInstance(getActivity());
        Service service = cache.get();
        if (service == null) {
            ServiceLoader loader = new ServiceLoader(new ServiceLoader.OnLoadListener() {
                @Override
                public void onLoad(Service service) {
                    ServiceCache cache = ServiceCache.getInstance(getActivity());
                    cache.set(service);
                    addTransportRouteLists();
                }
            });
            loader.load();
        } else {
            addTransportRouteLists();
        }
    }

    private void addTransportRouteLists() {
        final ServiceCache cache = ServiceCache.getInstance(getActivity());
        final Service service = cache.get();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        for (Transport transport: service.transports) {
            ft.add(R.id.route_list, new TransportRouteListFragment(transport, new TransportRouteListFragment.OnRouteCheckChangeListener() {
                @Override
                public void onRouteCheckChange(Route route, boolean isChecked) {
                    route.isChecked = isChecked;
                    cache.set(service);
                }
            }));
        }
        ft.commit();
    }
}
