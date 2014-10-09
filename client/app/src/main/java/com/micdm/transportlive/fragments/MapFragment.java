package com.micdm.transportlive.fragments;

import android.content.SharedPreferences;
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
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pools;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.micdm.transportlive.App;
import com.micdm.transportlive.R;
import com.micdm.transportlive.data.MapVehicle;
import com.micdm.transportlive.data.SelectedRoute;
import com.micdm.transportlive.data.service.Route;
import com.micdm.transportlive.data.service.Service;
import com.micdm.transportlive.data.service.Transport;
import com.micdm.transportlive.events.EventManager;
import com.micdm.transportlive.events.EventType;
import com.micdm.transportlive.events.events.LoadRoutesEvent;
import com.micdm.transportlive.events.events.LoadServiceEvent;
import com.micdm.transportlive.events.events.RemoveVehicleEvent;
import com.micdm.transportlive.events.events.RequestLoadRoutesEvent;
import com.micdm.transportlive.events.events.RequestLoadServiceEvent;
import com.micdm.transportlive.events.events.UnselectRouteEvent;
import com.micdm.transportlive.events.events.UpdateVehicleEvent;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment {

    private class MarkerBuilder {

        private static final int BITMAP_POOL_SIZE = 100;
        private static final int SHADOW_SIZE = 2;

        private final Matrix matrix = new Matrix();
        private final Rect bounds = new Rect();

        private final Bitmap original;
        private final Pools.Pool<Bitmap> bitmapPool;
        private final Map<Route, Paint> routePaints;
        private final Paint textPaint;

        public MarkerBuilder() {
            original = getOriginalBitmap();
            bitmapPool = getBitmapPool();
            routePaints = getRoutePaints();
            textPaint = getTextPaint();
        }

        private Bitmap getOriginalBitmap() {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_vehicle);
        }

        private Pools.Pool<Bitmap> getBitmapPool() {
            Pools.Pool<Bitmap> pool = new Pools.SimplePool<Bitmap>(BITMAP_POOL_SIZE);
            for (int i = 0; i < BITMAP_POOL_SIZE; i += 1) {
                pool.release(Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig()));
            }
            return pool;
        }

        private Map<Route, Paint> getRoutePaints() {
            Map<Route, Paint> paints = new HashMap<Route, Paint>();
            RouteColors colors = new RouteColors(service);
            for (Transport transport: service.getTransports()) {
                for (Route route: transport.getRoutes()) {
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

        public Bitmap build(MapVehicle vehicle) {
            Bitmap bitmap = bitmapPool.acquire();
            if (bitmap == null) {
                return null;
            }
            Canvas canvas = new Canvas(bitmap);
            matrix.setRotate(vehicle.getCourse(), original.getWidth() / 2, original.getHeight() / 2);
            Transport transport = service.getTransportById(vehicle.getTransportId());
            Route route = transport.getRouteByNumber(vehicle.getRouteNumber());
            canvas.drawBitmap(original, matrix, routePaints.get(route));
            String text = String.valueOf(route.getNumber());
            textPaint.getTextBounds(text, 0, text.length(), bounds);
            canvas.drawText(text, canvas.getWidth() / 2 - bounds.width() / 2, canvas.getHeight() / 2 + bounds.height() / 2 - SHADOW_SIZE, textPaint);
            return bitmap;
        }

        public void release(Bitmap bitmap) {
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

    private boolean externalMapUsed;
    private MarkerBuilder builder;
    private ItemizedIconOverlay<OverlayItem> overlay;
    private Map<MapVehicle, OverlayItem> items = new HashMap<MapVehicle, OverlayItem>();

    private Service service;

    private ViewGroup mapContainerView;
    private View noRouteSelectedView;
    private View noVehiclesView;
    private MapView mapView;
    private View zoomInView;
    private View zoomOutView;
    private View loadingView;

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
        zoomInView = view.findViewById(R.id.f__map__zoom_in);
        zoomInView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.getController().zoomIn();
            }
        });
        zoomOutView = view.findViewById(R.id.f__map__zoom_out);
        zoomOutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.getController().zoomOut();
            }
        });
        loadingView = view.findViewById(R.id.f__map__loading);
        View selectRoutesView = view.findViewById(R.id.f__map__select_route);
        selectRoutesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getChildFragmentManager();
                if (manager.findFragmentByTag(FragmentTag.SELECT_ROUTE) == null) {
                    (new SelectRouteFragment()).show(manager, FragmentTag.SELECT_ROUTE);
                    App.get().getAnalytics().reportEvent(Analytics.Category.DIALOGS, Analytics.Action.SHOW, "select_route");
                }
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
        subscribeForEvents();
        requestForData();
    }

    private void hideAllViews() {
        noRouteSelectedView.setVisibility(View.GONE);
        noVehiclesView.setVisibility(View.GONE);
        mapView.setVisibility(View.GONE);
        zoomInView.setVisibility(View.GONE);
        zoomOutView.setVisibility(View.GONE);
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
            App.get().getAnalytics().reportEvent(Analytics.Category.MISC, Analytics.Action.CLICK, "use_external_map");
        }
    }

    private void subscribeForEvents() {
        EventManager manager = App.get().getEventManager();
        manager.subscribe(this, EventType.LOAD_SERVICE, new EventManager.OnEventListener<LoadServiceEvent>() {
            @Override
            public void onEvent(LoadServiceEvent event) {
                service = event.getService();
                builder = new MarkerBuilder();
            }
        });
        manager.subscribe(this, EventType.LOAD_ROUTES, new EventManager.OnEventListener<LoadRoutesEvent>() {
            @Override
            public void onEvent(LoadRoutesEvent event) {
                if (event.getRoutes().size() == 0) {
                    hideAllViews();
                    noRouteSelectedView.setVisibility(View.VISIBLE);
                }
            }
        });
        manager.subscribe(this, EventType.UNSELECT_ROUTE, new EventManager.OnEventListener<UnselectRouteEvent>() {
            @Override
            public void onEvent(UnselectRouteEvent event) {
                SelectedRoute route = event.getRoute();
                int transportId = route.getTransportId();
                int routeNumber = route.getRouteNumber();
                List<String> numbers = new ArrayList<String>();
                for (MapVehicle vehicle: items.keySet()) {
                    if (vehicle.getTransportId() == transportId && vehicle.getRouteNumber() == routeNumber) {
                        numbers.add(vehicle.getNumber());
                    }
                }
                for (String number: numbers) {
                    removeVehicle(number);
                }
            }
        });
        manager.subscribe(this, EventType.UPDATE_VEHICLE, new EventManager.OnEventListener<UpdateVehicleEvent>() {
            @Override
            public void onEvent(UpdateVehicleEvent event) {
                updateVehicle(event.getVehicle());
            }
        });
        manager.subscribe(this, EventType.REMOVE_VEHICLE, new EventManager.OnEventListener<RemoveVehicleEvent>() {
            @Override
            public void onEvent(RemoveVehicleEvent event) {
                removeVehicle(event.getNumber());
            }
        });
    }

    private void requestForData() {
        EventManager manager = App.get().getEventManager();
        manager.publish(new RequestLoadServiceEvent());
        manager.publish(new RequestLoadRoutesEvent());
    }

    private void updateVehicle(MapVehicle vehicle) {
        if (mapView.getVisibility() != View.VISIBLE) {
            hideAllViews();
            mapView.setVisibility(View.VISIBLE);
            zoomInView.setVisibility(View.VISIBLE);
            zoomOutView.setVisibility(View.VISIBLE);
        }
        ItemizedIconOverlay<OverlayItem> overlay = getOverlay();
        Map.Entry<MapVehicle, OverlayItem> pair = getMapVehicleAndOverlayItemPair(vehicle.getNumber());
        if (pair != null) {
            OverlayItem item = pair.getValue();
            builder.release(((BitmapDrawable) item.getDrawable()).getBitmap());
            overlay.removeItem(item);
            items.remove(pair.getKey());
        }
        OverlayItem item = new OverlayItem(null, null, null, getVehicleGeoPoint(vehicle));
        item.setMarker(new BitmapDrawable(getResources(), builder.build(vehicle)));
        item.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
        overlay.addItem(item);
        items.put(vehicle, item);
        mapView.invalidate();
    }

    private void removeVehicle(String number) {
        ItemizedIconOverlay<OverlayItem> overlay = getOverlay();
        Map.Entry<MapVehicle, OverlayItem> pair = getMapVehicleAndOverlayItemPair(number);
        if (pair != null) {
            overlay.removeItem(pair.getValue());
            items.remove(pair.getKey());
            mapView.invalidate();
        }
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

    private Map.Entry<MapVehicle, OverlayItem> getMapVehicleAndOverlayItemPair(String number) {
        for (Map.Entry<MapVehicle, OverlayItem> item: items.entrySet()) {
            MapVehicle vehicle = item.getKey();
            if (vehicle.getNumber().equals(number)) {
                return item;
            }
        }
        return null;
    }

    private GeoPoint getVehicleGeoPoint(MapVehicle vehicle) {
        BigDecimal multiplier = new BigDecimal(1e6);
        int latitude = vehicle.getLatitude().multiply(multiplier).intValue();
        int longitude = vehicle.getLongitude().multiply(multiplier).intValue();
        return new GeoPoint(latitude, longitude);
    }

    @Override
    public void onStop() {
        super.onStop();
        App.get().getEventManager().unsubscribeAll(this);
    }
}
