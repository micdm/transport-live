package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pools;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.micdm.transportlive.CustomApplication;
import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.RouteInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.data.Vehicle;
import com.micdm.transportlive.interfaces.ServiceHandler;
import com.micdm.transportlive.misc.AssetArchive;
import com.micdm.transportlive.misc.RouteColors;
import com.micdm.transportlive.misc.analytics.Analytics;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment {

    private static class MarkerBuilder {

        private static final int BITMAP_POOL_SIZE = 100;
        private static final int SHADOW_SIZE = 2;

        private static final Matrix matrix = new Matrix();
        private static final Rect bounds = new Rect();

        private final Resources resources;
        private final Bitmap original;
        private final Pools.Pool<Bitmap> bitmapPool;
        private final Map<Route, Paint> routePaints;
        private final Paint textPaint;

        public MarkerBuilder(Service service, Resources resources) {
            this.resources = resources;
            original = getOriginalBitmap();
            bitmapPool = getBitmapPool();
            routePaints = getRoutePaints(service);
            textPaint = getTextPaint();
        }

        private Bitmap getOriginalBitmap() {
            return BitmapFactory.decodeResource(resources, R.drawable.ic_vehicle);
        }

        private Pools.Pool<Bitmap> getBitmapPool() {
            Pools.Pool<Bitmap> pool = new Pools.SimplePool<Bitmap>(BITMAP_POOL_SIZE);
            for (int i = 0; i < BITMAP_POOL_SIZE; i += 1) {
                pool.release(Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig()));
            }
            return pool;
        }

        private Map<Route, Paint> getRoutePaints(Service service) {
            Map<Route, Paint> paints = new HashMap<Route, Paint>();
            RouteColors colors = new RouteColors(service);
            for (Transport transport: service.transports) {
                for (Route route: transport.routes) {
                    paints.put(route, getRoutePaint(colors.get(route)));
                }
            }
            return paints;
        }

        private Paint getRoutePaint(int color) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            paint.setColorFilter(new LightingColorFilter(Color.BLACK, color));
            paint.setShadowLayer(SHADOW_SIZE, 0, SHADOW_SIZE, Color.BLACK);
            return paint;
        }

        private Paint getTextPaint() {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(original.getWidth() * 0.35f);
            paint.setShadowLayer(SHADOW_SIZE, 0, SHADOW_SIZE, Color.BLACK);
            return paint;
        }

        public List<OverlayItem> build(List<RouteInfo> routes) {
            List<OverlayItem> markers = new ArrayList<OverlayItem>();
            for (RouteInfo info: routes) {
                for (Vehicle vehicle: info.vehicles) {
                    OverlayItem marker = getMarker(info.route, vehicle);
                    if (marker != null) {
                        markers.add(marker);
                    }
                }
            }
            return markers;
        }

        private OverlayItem getMarker(Route route, Vehicle vehicle) {
            Bitmap bitmap = getBitmap(route, vehicle);
            if (bitmap == null) {
                return null;
            }
            GeoPoint coords = new GeoPoint(vehicle.latitude.doubleValue(), vehicle.longitude.doubleValue());
            OverlayItem marker = new OverlayItem(vehicle.number, "", coords);
            marker.setMarker(new BitmapDrawable(resources, bitmap));
            marker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
            return marker;
        }

        private Bitmap getBitmap(Route route, Vehicle vehicle) {
            Bitmap result = bitmapPool.acquire();
            if (result == null) {
                return null;
            }
            Canvas canvas = new Canvas(result);
            matrix.setRotate(vehicle.course, original.getWidth() / 2, original.getHeight() / 2);
            canvas.drawBitmap(original, matrix, routePaints.get(route));
            String text = String.valueOf(route.number);
            textPaint.getTextBounds(text, 0, text.length(), bounds);
            canvas.drawText(text, canvas.getWidth() / 2 - bounds.width() / 2, canvas.getHeight() / 2 + bounds.height() / 2 - SHADOW_SIZE, textPaint);
            return result;
        }

        public void release(OverlayItem marker) {
            Bitmap bitmap = ((BitmapDrawable) marker.getDrawable()).getBitmap();
            bitmap.eraseColor(Color.TRANSPARENT);
            bitmapPool.release(bitmap);
        }
    }

    private static final String PREF_KEY_USE_EXTERNAL_MAP = "pref_use_external_map";
    private static final int TILE_SIZE = 256;
    private static final int MIN_ZOOM = 14;
    private static final int MAX_ZOOM = 16;
    private static final int NORTH_EDGE = 56535258;
    private static final int WEST_EDGE = 84924316;
    private static final int SOUTH_EDGE = 56447313;
    private static final int EAST_EDGE = 85111084;
    private static final GeoPoint INITIAL_LOCATION = new GeoPoint(56484642, 84948100);

    private ServiceHandler serviceHandler;
    private final ServiceHandler.OnUnselectAllRoutesListener onUnselectAllRoutesListener = new ServiceHandler.OnUnselectAllRoutesListener() {
        @Override
        public void onUnselectAllRoutes() {
            hideAllViews();
            noRouteSelectedView.setVisibility(View.VISIBLE);
        }
    };
    private final ServiceHandler.OnLoadServiceListener onLoadServiceListener = new ServiceHandler.OnLoadServiceListener() {
        @Override
        public void onLoadService(Service service) {
            builder = new MarkerBuilder(service, getResources());
        }
    };
    private final ServiceHandler.OnLoadVehiclesListener onLoadVehiclesListener = new ServiceHandler.OnLoadVehiclesListener() {
        @Override
        public void onStart() {
            loadingView.setVisibility(View.VISIBLE);
        }
        @Override
        public void onFinish() {
            loadingView.setVisibility(View.GONE);
        }
        @Override
        public void onLoadVehicles(List<RouteInfo> vehicles) {
            update(vehicles);
        }
    };

    private boolean externalMapUsed;
    private MarkerBuilder builder;
    private ItemizedIconOverlay<OverlayItem> overlay;

    private ViewGroup mapContainerView;
    private View noRouteSelectedView;
    private View noVehiclesView;
    private MapView mapView;
    private View loadingView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        serviceHandler = (ServiceHandler) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f__map, container, false);
        mapContainerView = (ViewGroup) view.findViewById(R.id.f__map__map_container);
        noRouteSelectedView = view.findViewById(R.id.f__map__no_route_selected);
        noVehiclesView = view.findViewById(R.id.f__map__no_vehicles);
        externalMapUsed = needUseExternalMap();
        mapView = getMapView(externalMapUsed);
        mapView.setVisibility(View.GONE);
        mapContainerView.addView(mapView, 0);
        setupMapController(mapView);
        loadingView = view.findViewById(R.id.f__map__loading);
        View selectRoutesView = view.findViewById(R.id.f__map__select_route);
        selectRoutesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceHandler.requestRouteSelection();
            }
        });
        return view;
    }

    private boolean needUseExternalMap() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getBoolean(PREF_KEY_USE_EXTERNAL_MAP, false);
    }

    private MapView getMapView(boolean needUseExternalMap) {
        ResourceProxy proxy = new DefaultResourceProxyImpl(getActivity());
        MapView mapView = new MapView(getActivity(), TILE_SIZE, proxy, needUseExternalMap ? null : getTileProviders());
        mapView.setId(R.id.f__map__map);
        if (!needUseExternalMap) {
            mapView.setMinZoomLevel(MIN_ZOOM);
            mapView.setMaxZoomLevel(MAX_ZOOM);
        }
        mapView.setScrollableAreaLimit(new BoundingBoxE6(NORTH_EDGE, EAST_EDGE, SOUTH_EDGE, WEST_EDGE));
        mapView.setBuiltInZoomControls(true);
        return mapView;
    }

    private void setupMapController(MapView view) {
        IMapController controller = view.getController();
        controller.setZoom(MAX_ZOOM);
        controller.setCenter(INITIAL_LOCATION);
    }

    private MapTileProviderArray getTileProviders() {
        IRegisterReceiver receiver = new SimpleRegisterReceiver(getActivity());
        ITileSource source = new XYTileSource("OSMPublicTransport", ResourceProxy.string.public_transport, MIN_ZOOM, MAX_ZOOM, TILE_SIZE, ".jpg", null);
        AssetArchive archive = AssetArchive.getAssetArchive(getActivity());
        MapTileFileArchiveProvider provider = new MapTileFileArchiveProvider(receiver, source, new IArchiveFile[] {archive});
        return new MapTileProviderArray(source, receiver, new MapTileModuleProviderBase[] {provider});
    }

    @Override
    public void onStart() {
        super.onStart();
        hideAllViews();
        if (externalMapUsed != needUseExternalMap()) {
            switchMapSource();
        }
        serviceHandler.addOnUnselectAllRoutesListener(onUnselectAllRoutesListener);
        serviceHandler.addOnLoadServiceListener(onLoadServiceListener);
        serviceHandler.addOnLoadVehiclesListener(onLoadVehiclesListener);
    }

    private void hideAllViews() {
        noRouteSelectedView.setVisibility(View.GONE);
        noVehiclesView.setVisibility(View.GONE);
        mapView.setVisibility(View.GONE);
    }

    private void switchMapSource() {
        externalMapUsed = needUseExternalMap();
        MapView newMapView = getMapView(externalMapUsed);
        newMapView.setVisibility(mapView.getVisibility());
        mapContainerView.removeView(mapView);
        mapContainerView.addView(newMapView, 0);
        setupMapController(newMapView);
        List<Overlay> overlays = mapView.getOverlays();
        if (!overlays.isEmpty()) {
            newMapView.getOverlays().add(overlays.get(0));
        }
        mapView = newMapView;
        if (needUseExternalMap()) {
            CustomApplication.get().getAnalytics().reportEvent(Analytics.Category.MISC, Analytics.Action.CLICK, "use_external_map");
        }
    }

    public void update(List<RouteInfo> vehicles) {
        hideAllViews();
        if (vehicles.size() == 0) {
            noVehiclesView.setVisibility(View.VISIBLE);
            return;
        }
        ItemizedIconOverlay<OverlayItem> overlay = getOverlay();
        for (int i = 0; i < overlay.size(); i += 1) {
            builder.release(overlay.getItem(i));
        }
        overlay.removeAllItems();
        overlay.addItems(builder.build(vehicles));
        mapView.setVisibility(View.VISIBLE);
    }

    private ItemizedIconOverlay<OverlayItem> getOverlay() {
        if (overlay == null) {
            overlay = new ItemizedIconOverlay<OverlayItem>(getActivity(), new ArrayList<OverlayItem>(), new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                @Override
                public boolean onItemSingleTapUp(int i, OverlayItem item) {
                    return false;
                }
                @Override
                public boolean onItemLongPress(int i, OverlayItem item) {
                    return false;
                }
            });
            mapView.getOverlays().add(overlay);
        }
        return overlay;
    }

    @Override
    public void onStop() {
        super.onStop();
        serviceHandler.removeOnUnselectAllRoutesListener(onUnselectAllRoutesListener);
        serviceHandler.removeOnLoadServiceListener(onLoadServiceListener);
        serviceHandler.removeOnLoadVehiclesListener(onLoadVehiclesListener);
    }
}
