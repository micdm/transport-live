package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Point;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.data.Vehicle;
import com.micdm.transportlive.misc.ServiceHandler;

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

    private ServiceHandler handler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        handler = (ServiceHandler) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, null);
        if (view != null) {
            MapView map = (MapView) view.findViewById(R.id.map);
            map.setMultiTouchControls(true);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handler.setOnLoadVehiclesListener(new ServiceHandler.OnLoadVehiclesListener() {
            @Override
            public void onLoadVehicles(Service service) {
                update(service);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.setOnLoadVehiclesListener(null);
    }

    public void update(Service service) {
        Date now = new Date();
        ArrayList<OverlayItem> markers = new ArrayList<OverlayItem>();
        Rect bounds = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        for (Vehicle vehicle: getVehicles(service)) {
//            if (now.getTime() - vehicle.lastUpdate.getTime() > UPDATE_TIMEOUT) {
//                continue;
//            }
            markers.add(getMarker(vehicle));
            updateBounds(bounds, vehicle.location);
        }
        updateMap(markers, new BoundingBoxE6(bounds.top, bounds.right, bounds.bottom, bounds.left));
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

    private OverlayItem getMarker(Vehicle vehicle) {
        OverlayItem marker = new OverlayItem(vehicle.number, "", new GeoPoint(vehicle.location.latitude, vehicle.location.longitude));
        marker.setMarker(getResources().getDrawable(android.R.drawable.arrow_down_float));
        marker.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
        return marker;
    }

    private void updateBounds(Rect bounds, Point location) {
        bounds.left = Math.min(bounds.left, location.longitude);
        bounds.top = Math.min(bounds.top, location.latitude);
        bounds.right = Math.max(bounds.right, location.longitude);
        bounds.bottom = Math.max(bounds.bottom, location.latitude);
    }

    private Overlay getLayer(List<OverlayItem> markers) {
        return new ItemizedIconOverlay<OverlayItem>(getActivity(), markers, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int i, OverlayItem item) {
                return false;
            }
            @Override
            public boolean onItemLongPress(int i, OverlayItem item) {
                return false;
            }
        });
    }

    private void updateMap(List<OverlayItem> markers, BoundingBoxE6 bounds) {
        MapView map = (MapView) getView().findViewById(R.id.map);
        List<Overlay> overlays = map.getOverlays();
        overlays.clear();
        overlays.add(getLayer(markers));
        map.zoomToBoundingBox(bounds);
    }
}
