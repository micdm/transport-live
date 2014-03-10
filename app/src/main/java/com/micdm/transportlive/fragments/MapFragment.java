package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.data.Vehicle;
import com.micdm.transportlive.misc.ServiceHandler;

import org.apache.commons.io.IOUtils;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.ZipFileArchive;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private static final String ATLAS_FILE_NAME = "atlas.zip";
    private static final int TILE_SIZE = 256;
    private static final int MIN_ZOOM = 14;
    private static final int MAX_ZOOM = 15;
    private static final int NORTH = 56547372;
    private static final int WEST = 84902344;
    private static final int SOUTH = 56438204;
    private static final int EAST = 85122070;

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
            ResourceProxy proxy = new DefaultResourceProxyImpl(getActivity());
            MapTileProviderArray providers = getTileProviders();
            MapView mapView = new MapView(getActivity(), TILE_SIZE, proxy, providers);
            mapView.setId(R.id.map);
            mapView.setVisibility(View.GONE);
            mapView.setMinZoomLevel(MIN_ZOOM);
            mapView.setMaxZoomLevel(MAX_ZOOM);
            mapView.setScrollableAreaLimit(new BoundingBoxE6(NORTH, EAST, SOUTH, WEST));
            mapView.setMultiTouchControls(true);
            ((ViewGroup) view).addView(mapView);
        }
        return view;
    }

    private MapTileProviderArray getTileProviders() {
        try {
            IRegisterReceiver receiver = new SimpleRegisterReceiver(getActivity());
            ITileSource source = new XYTileSource("OSMPublicTransport", ResourceProxy.string.public_transport, MIN_ZOOM, MAX_ZOOM, TILE_SIZE, ".png", null);
            ZipFileArchive archive = ZipFileArchive.getZipFileArchive(getAtlas());
            MapTileFileArchiveProvider provider = new MapTileFileArchiveProvider(receiver, source, new IArchiveFile[] {archive});
            return new MapTileProviderArray(source, receiver, new MapTileModuleProviderBase[] {provider});
        } catch (IOException e) {
            throw new RuntimeException("cannot load atlas");
        }
    }

    private File getAtlas() {
        File file = new File(new File(getActivity().getCacheDir(), ATLAS_FILE_NAME).getAbsolutePath());
        if (!file.exists()) {
            copyAtlasToCache(file);
        }
        return file;
    }

    private void copyAtlasToCache(File file) {
        try {
            InputStream input = getActivity().getAssets().open(ATLAS_FILE_NAME);
            OutputStream output = new FileOutputStream(file);
            IOUtils.copy(input, output);
            input.close();
            output.close();
        } catch (IOException e) {
            throw new RuntimeException("cannot copy atlas to cache");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        hideViews();
        handler.setOnUnselectAllRoutesListener(new ServiceHandler.OnUnselectAllRoutesListener() {
            @Override
            public void onUnselectAllRoutes() {
                hideViews();
                showView(R.id.no_route_selected);
            }
        });
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
        hideViews();
        Vehicle[] vehicles = getVehicles(service);
        if (vehicles.length == 0) {
            showView(R.id.no_vehicles);
            return;
        }
        showView(R.id.map);
        ArrayList<OverlayItem> markers = new ArrayList<OverlayItem>();
        for (Vehicle vehicle: vehicles) {
            markers.add(getMarker(vehicle));
        }
        updateMap(markers);
    }

    private void showView(int id) {
        getView().findViewById(id).setVisibility(View.VISIBLE);
    }

    private void hideViews() {
        ViewGroup container = ((ViewGroup) ((ViewGroup) getView()).getChildAt(0));
        if (container == null) {
            return;
        }
        for (int i = 0; i < container.getChildCount(); i += 1) {
            View view = container.getChildAt(i);
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
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

    private void updateMap(List<OverlayItem> markers) {
        MapView view = (MapView) getView().findViewById(R.id.map);
        if (view != null) {
            List<Overlay> overlays = view.getOverlays();
            overlays.clear();
            overlays.add(getLayer(markers));
        }
    }
}
