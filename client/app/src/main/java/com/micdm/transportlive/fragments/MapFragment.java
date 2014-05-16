package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
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
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.VehicleInfo;
import com.micdm.transportlive.interfaces.ServiceHandler;
import com.micdm.transportlive.misc.AssetArchive;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
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

        public List<OverlayItem> build(List<VehicleInfo> vehicles) {
            List<OverlayItem> markers = new ArrayList<OverlayItem>();
            for (VehicleInfo info: vehicles) {
                // TODO: учитывать lastUpdate
                markers.add(getMarker(info));
            }
            return markers;
        }

        private OverlayItem getMarker(VehicleInfo info) {
            GeoPoint coords = new GeoPoint(info.vehicle.latitude.doubleValue(), info.vehicle.longitude.doubleValue());
            OverlayItem marker = new OverlayItem(info.vehicle.number, "", coords);
            Bitmap bitmap = getBitmap(info);
            marker.setMarker(new BitmapDrawable(resources, bitmap));
            marker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
            return marker;
        }

        private Bitmap getBitmap(VehicleInfo info) {
            Bitmap result = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
            Canvas canvas = new Canvas(result);
            Matrix matrix = new Matrix();
            matrix.setRotate(info.vehicle.course, original.getWidth() / 2, original.getHeight() / 2);
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

    private static final String PREF_KEY_USE_EXTERNAL_MAP = "pref_use_external_map";
    private static final int TILE_SIZE = 256;
    private static final int MIN_ZOOM = 14;
    private static final int MAX_ZOOM = 15;
    private static final int NORTH_EDGE = 56547372;
    private static final int WEST_EDGE = 84902344;
    private static final int SOUTH_EDGE = 56438204;
    private static final int EAST_EDGE = 85122070;
    private static final GeoPoint INITIAL_LOCATION = new GeoPoint(56484642, 84948100);

    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (!key.equals(PREF_KEY_USE_EXTERNAL_MAP)) {
                return;
            }
            ViewGroup containerView = ((ViewGroup) getView().findViewById(R.id.map_container));
            MapView oldMapView = (MapView) containerView.getChildAt(0);
            MapView newMapView = getMapView(needUseExternalMap());
            newMapView.setVisibility(oldMapView.getVisibility());
            containerView.removeView(oldMapView);
            containerView.addView(newMapView, 0);
            setupMapController(newMapView);
            List<Overlay> overlays = oldMapView.getOverlays();
            if (!overlays.isEmpty()) {
                newMapView.getOverlays().add(overlays.get(0));
            }
        }
    };

    private ServiceHandler serviceHandler;
    private ServiceHandler.OnUnselectAllRoutesListener onUnselectAllRoutesListener = new ServiceHandler.OnUnselectAllRoutesListener() {
        @Override
        public void onUnselectAllRoutes() {
            hideAllViews();
            showView(R.id.no_route_selected);
        }
    };
    private ServiceHandler.OnLoadVehiclesListener onLoadVehiclesListener = new ServiceHandler.OnLoadVehiclesListener() {
        @Override
        public void onStart() {
            showView(R.id.loading);
        }
        @Override
        public void onFinish() {
            hideView(R.id.loading);
        }
        @Override
        public void onLoadVehicles(List<VehicleInfo> vehicles) {
            update(vehicles);
        }
    };

    private MarkerBuilder builder;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        serviceHandler = (ServiceHandler) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, null);
        if (view != null) {
            View selectRoutesView = view.findViewById(R.id.select_route);
            selectRoutesView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    serviceHandler.requestRouteSelection();
                }
            });
            MapView mapView = getMapView(needUseExternalMap());
            mapView.setVisibility(View.GONE);
            ((ViewGroup) view.findViewById(R.id.map_container)).addView(mapView, 0);
            setupMapController(mapView);
        }
        return view;
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    private boolean needUseExternalMap() {
        return getSharedPreferences().getBoolean(PREF_KEY_USE_EXTERNAL_MAP, false);
    }

    private MapView getMapView(boolean needUseExternalMap) {
        ResourceProxy proxy = new DefaultResourceProxyImpl(getActivity());
        MapView mapView = new MapView(getActivity(), TILE_SIZE, proxy, needUseExternalMap ? null : getTileProviders());
        mapView.setId(R.id.map);
        if (!needUseExternalMap) {
            mapView.setMinZoomLevel(MIN_ZOOM);
            mapView.setMaxZoomLevel(MAX_ZOOM);
        }
        mapView.setScrollableAreaLimit(new BoundingBoxE6(NORTH_EDGE, EAST_EDGE, SOUTH_EDGE, WEST_EDGE));
        mapView.setMultiTouchControls(true);
        return mapView;
    }

    private void setupMapController(MapView view) {
        IMapController controller = view.getController();
        controller.setZoom(MAX_ZOOM);
        controller.setCenter(INITIAL_LOCATION);
    }

    private MapTileProviderArray getTileProviders() {
        IRegisterReceiver receiver = new SimpleRegisterReceiver(getActivity());
        ITileSource source = new XYTileSource("OSMPublicTransport", ResourceProxy.string.public_transport, MIN_ZOOM, MAX_ZOOM, TILE_SIZE, ".png", null);
        AssetArchive archive = AssetArchive.getAssetArchive(getActivity());
        MapTileFileArchiveProvider provider = new MapTileFileArchiveProvider(receiver, source, new IArchiveFile[] {archive});
        return new MapTileProviderArray(source, receiver, new MapTileModuleProviderBase[] {provider});
    }

    @Override
    public void onStart() {
        super.onStart();
        hideAllViews();
        getSharedPreferences().registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        serviceHandler.addOnUnselectAllRoutesListener(onUnselectAllRoutesListener);
        serviceHandler.addOnLoadVehiclesListener(onLoadVehiclesListener);
    }

    private void showView(int id) {
        getView().findViewById(id).setVisibility(View.VISIBLE);
    }

    private void hideView(int id) {
        getView().findViewById(id).setVisibility(View.GONE);
    }

    private void hideAllViews() {
        hideView(R.id.no_route_selected);
        hideView(R.id.no_vehicles);
        hideView(R.id.map);
    }

    public void update(List<VehicleInfo> vehicles) {
        hideAllViews();
        if (vehicles.size() == 0) {
            showView(R.id.no_vehicles);
            return;
        }
        showView(R.id.map);
        if (builder == null) {
            builder = new MarkerBuilder(getResources());
        }
        List<OverlayItem> markers = builder.build(vehicles);
        updateMap(markers);
    }

    private void updateMap(List<OverlayItem> markers) {
        MapView view = (MapView) getView().findViewById(R.id.map);
        if (view != null) {
            List<Overlay> overlays = view.getOverlays();
            overlays.clear();
            overlays.add(getLayer(markers));
        }
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

    @Override
    public void onStop() {
        super.onStop();
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        serviceHandler.removeOnUnselectAllRoutesListener(onUnselectAllRoutesListener);
        serviceHandler.removeOnLoadVehiclesListener(onLoadVehiclesListener);
    }
}
