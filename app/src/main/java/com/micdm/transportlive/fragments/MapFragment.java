package com.micdm.transportlive.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.misc.ServiceCache;
import com.micdm.transportlive.misc.ServiceLoader;
import com.micdm.transportlive.server.ServerConnectTask;
import com.micdm.transportlive.server.commands.Command;
import com.micdm.transportlive.server.commands.GetVehiclesCommand;

public class MapFragment extends Fragment {

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        ServiceCache cache = ServiceCache.getInstance(getActivity());
        Service service = cache.get();
        if (service == null) {
            ServiceLoader loader = new ServiceLoader(new ServiceLoader.OnLoadListener() {
                @Override
                public void onLoad(Service service) {
                    ServiceCache cache = ServiceCache.getInstance(getActivity());
                    cache.set(service);
                    loadVehicles();
                }
            });
            loader.load();
        } else {
            loadVehicles();
        }
    }

    private void loadVehicles() {
        ServiceCache cache = ServiceCache.getInstance(getActivity());
        Service service = cache.get();
        ServerConnectTask task = new ServerConnectTask(new ServerConnectTask.OnResultListener() {
            @Override
            public void onResult(Command.Result result) {

            }
        });
        task.execute(new GetVehiclesCommand(service));
    }
}
