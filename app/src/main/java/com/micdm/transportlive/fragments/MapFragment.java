package com.micdm.transportlive.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.micdm.transportlive.MainActivity;
import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.data.Vehicle;
import com.micdm.transportlive.server.ServerConnectTask;
import com.micdm.transportlive.server.commands.Command;
import com.micdm.transportlive.server.commands.GetVehiclesCommand;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapFragment extends Fragment {

    private static final int UPDATE_TIMEOUT = 30;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadVehicles();
    }

    private void loadVehicles() {
        Service service = ((MainActivity) getActivity()).getService();
        ServerConnectTask task = new ServerConnectTask(new ServerConnectTask.OnResultListener() {
            @Override
            public void onResult(Command.Result result) {
                updateMarkers(((GetVehiclesCommand.Result)result).service);
            }
        });
        task.execute(new GetVehiclesCommand(service));
    }

    private void updateMarkers(Service service) {
        Date now = new Date();
        ArrayList<OverlayItem> markers = new ArrayList<OverlayItem>();
        Rect box = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0);
        for (Vehicle vehicle: getVehicles(service)) {
            if (now.getTime() - vehicle.lastUpdate.getTime() > UPDATE_TIMEOUT) {
                // TODO: убирать с карты устаревшие метки
            }
            OverlayItem marker = new OverlayItem(vehicle.number, "", new GeoPoint(vehicle.location.latitude, vehicle.location.longitude));
            marker.setMarker(getResources().getDrawable(android.R.drawable.arrow_down_float));
            marker.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
            markers.add(marker);
            box.left = Math.min(box.left, vehicle.location.longitude);
            box.top = Math.min(box.top, vehicle.location.latitude);
            box.right = Math.max(box.right, vehicle.location.longitude);
            box.bottom = Math.max(box.bottom, vehicle.location.latitude);
        }
        Overlay overlay = new ItemizedIconOverlay<OverlayItem>(getActivity(), markers, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int i, OverlayItem item) {
                return false;
            }
            @Override
            public boolean onItemLongPress(int i, OverlayItem item) {
                return false;
            }
        });
        MapView map = (MapView)getView().findViewById(R.id.map);
        List<Overlay> overlays = map.getOverlays();
        overlays.clear();
        overlays.add(overlay);
        map.zoomToBoundingBox(new BoundingBoxE6(box.top, box.right, box.bottom, box.left));
        map.setMultiTouchControls(true);
    }

    private Vehicle[] getVehicles(Service service) {
        ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
        for (Transport transport: service.transports) {
            for (Route route: transport.routes) {
                for (Direction direction: route.directions) {
                    vehicles.addAll(direction.vehicles);
                }
            }
        }
        return vehicles.toArray(new Vehicle[vehicles.size()]);
    }
}
