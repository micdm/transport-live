package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
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
import com.micdm.transportlive.data.VehicleInfo;
import com.micdm.transportlive.misc.ServiceHandler;

import org.apache.commons.io.IOUtils;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
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
import java.util.Collections;
import java.util.List;

public class MapFragment extends Fragment {

    private static class MarkerBuilder {

        private Resources resources;
        private Bitmap original;
        private Paint paint;

        public MarkerBuilder(Resources resources) {
            this.resources = resources;
            setupOriginalBitmap();
            setupPaint();
        }

        private void setupOriginalBitmap() {
            original = BitmapFactory.decodeResource(resources, R.drawable.vehicle);
        }

        private void setupPaint() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(original.getWidth() / 2);
        }

        public OverlayItem[] build(VehicleInfo[] vehicles) {
            ArrayList<OverlayItem> markers = new ArrayList<OverlayItem>();
            for (VehicleInfo info: vehicles) {
                markers.add(getMarker(info));
            }
            return markers.toArray(new OverlayItem[markers.size()]);
        }

        private OverlayItem getMarker(VehicleInfo info) {
            OverlayItem marker = new OverlayItem(info.vehicle.number, "", new GeoPoint(info.vehicle.location.latitude, info.vehicle.location.longitude));
            Bitmap bitmap = getBitmap(info);
            marker.setMarker(new BitmapDrawable(resources, bitmap));
            marker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
            return marker;
        }

        private Bitmap getBitmap(VehicleInfo info) {
            Bitmap result = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
            Canvas canvas = new Canvas(result);
            Matrix matrix = new Matrix();
            matrix.setRotate(info.vehicle.direction, original.getWidth() / 2, original.getHeight() / 2);
            canvas.drawBitmap(original, matrix, null);
            String text = String.valueOf(info.route.number);
            Rect bounds = getTextBounds(text);
            canvas.drawText(text, canvas.getWidth() / 2 - bounds.width() / 2, canvas.getHeight() / 2 + bounds.height() / 2, paint);
            return result;
        }

        private Rect getTextBounds(String text) {
            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            return bounds;
        }
    }

    private static final String ATLAS_FILE_NAME = "atlas.zip";
    private static final int TILE_SIZE = 256;
    private static final int MIN_ZOOM = 14;
    private static final int MAX_ZOOM = 15;
    private static final int NORTH_EDGE = 56547372;
    private static final int WEST_EDGE = 84902344;
    private static final int SOUTH_EDGE = 56438204;
    private static final int EAST_EDGE = 85122070;
    private static final GeoPoint INITIAL_LOCATION = new GeoPoint(56484642, 84948100);

    private ServiceHandler handler;
    private MarkerBuilder builder;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        handler = (ServiceHandler) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, null);
        if (view != null) {
            MapView mapView = getMapView();
            ((ViewGroup) view).addView(mapView);
            IMapController controller = mapView.getController();
            controller.setZoom(MAX_ZOOM);
            controller.setCenter(INITIAL_LOCATION);
        }
        return view;
    }

    private MapView getMapView() {
        ResourceProxy proxy = new DefaultResourceProxyImpl(getActivity());
        MapTileProviderArray providers = getTileProviders();
        MapView mapView = new MapView(getActivity(), TILE_SIZE, proxy, providers);
        mapView.setId(R.id.map);
        mapView.setVisibility(View.GONE);
        mapView.setMinZoomLevel(MIN_ZOOM);
        mapView.setMaxZoomLevel(MAX_ZOOM);
        mapView.setScrollableAreaLimit(new BoundingBoxE6(NORTH_EDGE, EAST_EDGE, SOUTH_EDGE, WEST_EDGE));
        mapView.setMultiTouchControls(true);
        return mapView;
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
        handler.setOnUnselectAllRoutesListener(null);
        handler.setOnLoadVehiclesListener(null);
    }

    public void update(Service service) {
        hideViews();
        VehicleInfo[] vehicles = getVehicles(service);
        if (vehicles.length == 0) {
            showView(R.id.no_vehicles);
            return;
        }
        showView(R.id.map);
        if (builder == null) {
            builder = new MarkerBuilder(getResources());
        }
        OverlayItem[] markers = builder.build(vehicles);
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

    private VehicleInfo[] getVehicles(Service service) {
        ArrayList<VehicleInfo> vehicles = new ArrayList<VehicleInfo>();
        for (Transport transport: service.transports) {
            for (Route route: transport.routes) {
                for (Direction direction: route.directions) {
                    for (Vehicle vehicle: direction.vehicles) {
                        vehicles.add(new VehicleInfo(route, vehicle));
                    }
                }
            }
        }
        return vehicles.toArray(new VehicleInfo[vehicles.size()]);
    }

    private void updateMap(OverlayItem[] markers) {
        MapView view = (MapView) getView().findViewById(R.id.map);
        if (view != null) {
            List<Overlay> overlays = view.getOverlays();
            overlays.clear();
            overlays.add(getLayer(markers));
        }
    }

    private Overlay getLayer(OverlayItem[] markers) {
        ArrayList<OverlayItem> list = new ArrayList<OverlayItem>();
        Collections.addAll(list, markers);
        return new ItemizedIconOverlay<OverlayItem>(getActivity(), list, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
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
}
