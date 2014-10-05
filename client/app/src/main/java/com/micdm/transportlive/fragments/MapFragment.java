package com.micdm.transportlive.fragments;

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
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pools;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.micdm.transportlive.App;
import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.RoutePopulation;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.data.Vehicle;
import com.micdm.transportlive.events.EventManager;
import com.micdm.transportlive.events.EventType;
import com.micdm.transportlive.events.events.LoadRoutesEvent;
import com.micdm.transportlive.events.events.LoadServiceEvent;
import com.micdm.transportlive.events.events.LoadVehiclesEvent;
import com.micdm.transportlive.events.events.RequestLoadRoutesEvent;
import com.micdm.transportlive.events.events.RequestLoadServiceEvent;
import com.micdm.transportlive.events.events.RequestLoadVehiclesEvent;
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
        private final Map<Integer, Paint> routePaints;
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

        private Map<Integer, Paint> getRoutePaints(Service service) {
            Map<Integer, Paint> paints = new HashMap<Integer, Paint>();
            RouteColors colors = new RouteColors(service);
            for (Transport transport: service.getTransports()) {
                for (Route route: transport.getRoutes()) {
                    paints.put(route.getNumber(), getRoutePaint(colors.get(route.getNumber())));
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

        public List<OverlayItem> build(List<RoutePopulation> populations) {
            List<OverlayItem> markers = new ArrayList<OverlayItem>();
            for (RoutePopulation population: populations) {
                for (Vehicle vehicle: population.getVehicles()) {
                    OverlayItem marker = getMarker(population.getRouteNumber(), vehicle);
                    if (marker != null) {
                        markers.add(marker);
                    }
                }
            }
            return markers;
        }

        private OverlayItem getMarker(int routeNumber, Vehicle vehicle) {
            Bitmap bitmap = getBitmap(routeNumber, vehicle);
            if (bitmap == null) {
                return null;
            }
            GeoPoint coords = new GeoPoint(vehicle.getLatitude().doubleValue(), vehicle.getLongitude().doubleValue());
            OverlayItem marker = new OverlayItem(vehicle.getNumber(), "", coords);
            marker.setMarker(new BitmapDrawable(resources, bitmap));
            marker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
            return marker;
        }

        private Bitmap getBitmap(int routeNumber, Vehicle vehicle) {
            Bitmap result = bitmapPool.acquire();
            if (result == null) {
                return null;
            }
            Canvas canvas = new Canvas(result);
            matrix.setRotate(vehicle.getCourse(), original.getWidth() / 2, original.getHeight() / 2);
            canvas.drawBitmap(original, matrix, routePaints.get(routeNumber));
            String text = String.valueOf(routeNumber);
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

    private static final String SELECT_ROUTE_FRAGMENT_TAG = "select_route";

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
                if (manager.findFragmentByTag(SELECT_ROUTE_FRAGMENT_TAG) == null) {
                    (new SelectRouteFragment()).show(manager, SELECT_ROUTE_FRAGMENT_TAG);
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
                builder = new MarkerBuilder(event.getService(), getResources());
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
        manager.subscribe(this, EventType.LOAD_VEHICLES, new EventManager.OnEventListener<LoadVehiclesEvent>() {
            @Override
            public void onEvent(LoadVehiclesEvent event) {
                switch (event.getState()) {
                    case LoadVehiclesEvent.STATE_START:
                        loadingView.setVisibility(View.VISIBLE);
                        break;
                    case LoadVehiclesEvent.STATE_FINISH:
                        loadingView.setVisibility(View.GONE);
                        break;
                    case LoadVehiclesEvent.STATE_COMPLETE:
                        update(event.getPopulations());
                        break;
                }
            }
        });
    }

    private void requestForData() {
        EventManager manager = App.get().getEventManager();
        manager.publish(new RequestLoadServiceEvent());
        manager.publish(new RequestLoadRoutesEvent());
        manager.publish(new RequestLoadVehiclesEvent());
    }

    public void update(List<RoutePopulation> populations) {
        hideAllViews();
        if (populations.size() == 0) {
            noVehiclesView.setVisibility(View.VISIBLE);
            return;
        }
        ItemizedIconOverlay<OverlayItem> overlay = getOverlay();
        for (int i = 0; i < overlay.size(); i += 1) {
            builder.release(overlay.getItem(i));
        }
        overlay.removeAllItems();
        overlay.addItems(builder.build(populations));
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
        App.get().getEventManager().unsubscribeAll(this);
    }
}
