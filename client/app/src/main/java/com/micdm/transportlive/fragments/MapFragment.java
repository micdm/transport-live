package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pools;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.micdm.transportlive.events.events.RemoveAllDataEvent;
import com.micdm.transportlive.events.events.RemoveVehicleEvent;
import com.micdm.transportlive.events.events.RequestFocusVehicleEvent;
import com.micdm.transportlive.events.events.RequestLoadRoutesEvent;
import com.micdm.transportlive.events.events.RequestLoadServiceEvent;
import com.micdm.transportlive.events.events.UnselectRouteEvent;
import com.micdm.transportlive.events.events.UpdateVehicleEvent;
import com.micdm.transportlive.misc.RouteColors;
import com.micdm.transportlive.misc.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment {

    private static class MarkerInfo {

        public final Marker marker;
        public Bitmap icon;

        private MarkerInfo(Marker marker) {
            this.marker = marker;
        }
    }

    private static class MarkerIconBuilder {

        private static final int BITMAP_POOL_SIZE = 200;
        private static final int SHADOW_SIZE = 2;

        private final Matrix matrix = new Matrix();
        private final Rect bounds = new Rect();

        private final Bitmap original;
        private final Pools.Pool<Bitmap> bitmapPool = new Pools.SimplePool<>(BITMAP_POOL_SIZE);
        private final Map<Bitmap, Canvas> canvases = new HashMap<>();
        private final Paint textPaint;
        private Map<Route, Paint> routePaints;

        public MarkerIconBuilder(Context context) {
            original = getOriginalBitmap(context);
            textPaint = getTextPaint();
        }

        private Bitmap getOriginalBitmap(Context context) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_vehicle);
        }

        private Paint getTextPaint() {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(original.getWidth() * 0.35f);
            paint.setShadowLayer(SHADOW_SIZE, 0, SHADOW_SIZE, Color.BLACK);
            return paint;
        }

        public void update(Service service) {
            routePaints = getRoutePaints(service);
        }

        private Map<Route, Paint> getRoutePaints(Service service) {
            Map<Route, Paint> paints = new HashMap<>();
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

        public Bitmap build(Service service, MapVehicle vehicle) {
            Bitmap bitmap = bitmapPool.acquire();
            if (bitmap == null) {
                bitmap = getBitmap();
            }
            Canvas canvas = getCanvas(bitmap);
            matrix.setRotate(vehicle.getCourse(), original.getWidth() / 2, original.getHeight() / 2);
            Transport transport = service.getTransportById(vehicle.getTransportId());
            Route route = transport.getRouteByNumber(vehicle.getRouteNumber());
            canvas.drawBitmap(original, matrix, routePaints.get(route));
            String text = String.valueOf(route.getNumber());
            textPaint.getTextBounds(text, 0, text.length(), bounds);
            canvas.drawText(text, canvas.getWidth() / 2 - bounds.width() / 2, canvas.getHeight() / 2 + bounds.height() / 2 - SHADOW_SIZE, textPaint);
            return bitmap;
        }

        private Bitmap getBitmap() {
            return Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        }

        private Canvas getCanvas(Bitmap bitmap) {
            Canvas canvas = canvases.get(bitmap);
            if (canvas == null) {
                canvas = new Canvas(bitmap);
                canvases.put(bitmap, canvas);
            }
            return canvas;
        }

        public void release(Bitmap bitmap) {
            bitmap.eraseColor(Color.TRANSPARENT);
            bitmapPool.release(bitmap);
        }
    }

    private final static double CAMERA_LATITUDE = 56.484642;
    private final static double CAMERA_LONGITUDE = 84.948100;
    private final static int CAMERA_ZOOM = 14;

    private MarkerIconBuilder markerIconBuilder;
    private final Map<MapVehicle, MarkerInfo> markers = new HashMap<>();

    private Service service;
    private List<SelectedRoute> selectedRoutes;

    private View noRouteSelectedView;
    private View noVehiclesView;
    private MapView mapView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        MapsInitializer.initialize(activity);
        markerIconBuilder = new MarkerIconBuilder(activity);
        mapView = new MapView(activity, getMapOptions());
    }

    private GoogleMapOptions getMapOptions() {
        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_NORMAL);
        options.camera(CameraPosition.fromLatLngZoom(new LatLng(CAMERA_LATITUDE, CAMERA_LONGITUDE), CAMERA_ZOOM));
        options.rotateGesturesEnabled(false);
        options.tiltGesturesEnabled(false);
        return options;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapView.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.f__map, container, false);
        noRouteSelectedView = view.findViewById(R.id.f__map__no_route_selected);
        noVehiclesView = view.findViewById(R.id.f__map__no_vehicles);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMyLocationEnabled(true);
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        return true;
                    }
                });
            }
        });
        view.addView(mapView);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        hideAllViews();
        subscribeForEvents();
        requestForData();
    }

    private void hideAllViews() {
        noRouteSelectedView.setVisibility(View.GONE);
        noVehiclesView.setVisibility(View.GONE);
        mapView.setVisibility(View.VISIBLE);
    }

    private void showNoRouteSelectedView() {
        hideAllViews();
        noRouteSelectedView.setVisibility(View.VISIBLE);
    }

    private void showNoVehiclesView() {
        hideAllViews();
        noVehiclesView.setVisibility(View.VISIBLE);
    }

    private void showMapView() {
        hideAllViews();
        mapView.setVisibility(View.VISIBLE);
    }

    private void subscribeForEvents() {
        EventManager manager = App.get().getEventManager();
        manager.subscribe(this, EventType.LOAD_SERVICE, new EventManager.OnEventListener<LoadServiceEvent>() {
            @Override
            public void onEvent(LoadServiceEvent event) {
                service = event.getService();
                markerIconBuilder.update(service);
            }
        });
        manager.subscribe(this, EventType.REMOVE_ALL_DATA, new EventManager.OnEventListener<RemoveAllDataEvent>() {
            @Override
            public void onEvent(RemoveAllDataEvent event) {
                removeAllVehicles();
                showNoVehiclesView();
            }
        });
        manager.subscribe(this, EventType.LOAD_ROUTES, new EventManager.OnEventListener<LoadRoutesEvent>() {
            @Override
            public void onEvent(LoadRoutesEvent event) {
                selectedRoutes = event.getRoutes();
                if (selectedRoutes.isEmpty()) {
                    showNoRouteSelectedView();
                }
            }
        });
        manager.subscribe(this, EventType.UNSELECT_ROUTE, new EventManager.OnEventListener<UnselectRouteEvent>() {
            @Override
            public void onEvent(UnselectRouteEvent event) {
                SelectedRoute route = event.getRoute();
                int transportId = route.getTransportId();
                int routeNumber = route.getRouteNumber();
                removeVehiclesByTransportAndRoute(transportId, routeNumber);
                if (markers.isEmpty()) {
                    showNoVehiclesView();
                }
            }
        });
        manager.subscribe(this, EventType.UPDATE_VEHICLE, new EventManager.OnEventListener<UpdateVehicleEvent>() {
            @Override
            public void onEvent(UpdateVehicleEvent event) {
                updateVehicle(event.getVehicle());
                showMapView();
            }
        });
        manager.subscribe(this, EventType.REMOVE_VEHICLE, new EventManager.OnEventListener<RemoveVehicleEvent>() {
            @Override
            public void onEvent(RemoveVehicleEvent event) {
                removeVehicle(event.getNumber());
                if (markers.isEmpty()) {
                    showNoVehiclesView();
                }
            }
        });
        manager.subscribe(this, EventType.REQUEST_FOCUS_VEHICLE, new EventManager.OnEventListener<RequestFocusVehicleEvent>() {
            @Override
            public void onEvent(RequestFocusVehicleEvent event) {
                int transportId = event.getTransportId();
                int routeNumber = event.getRouteNumber();
                if (Utils.isRouteSelected(selectedRoutes, transportId, routeNumber)) {
                    Map.Entry<MapVehicle, MarkerInfo> pair = getMapVehicleAndMarkerPair(event.getNumber());
                    if (pair != null) {
                        final MapVehicle vehicle = pair.getKey();
                        mapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                googleMap.animateCamera(CameraUpdateFactory.newLatLng(getGeoPoint(vehicle)));
                            }
                        });
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.f__map__route_not_selected, String.valueOf(routeNumber)), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateVehicle(final MapVehicle vehicle) {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Map.Entry<MapVehicle, MarkerInfo> pair = getMapVehicleAndMarkerPair(vehicle.getNumber());
                MarkerInfo markerInfo = (pair == null) ? null : pair.getValue();
                if (markerInfo == null) {
                    markerInfo = new MarkerInfo(googleMap.addMarker(getMarkerOptions()));
                    markers.put(vehicle, markerInfo);
                }
                Marker marker = markerInfo.marker;
                marker.setPosition(getGeoPoint(vehicle));
                Bitmap icon = markerInfo.icon;
                if (icon != null) {
                    markerIconBuilder.release(icon);
                }
                icon = markerIconBuilder.build(service, vehicle);
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
                markerInfo.icon = icon;
            }
        });
    }

    private MarkerOptions getMarkerOptions() {
        MarkerOptions options = new MarkerOptions();
        options.anchor(0.5f, 0.5f);
        options.position(new LatLng(0, 0));
        return options;
    }

    private void removeVehicle(String number) {
        Map.Entry<MapVehicle, MarkerInfo> pair = getMapVehicleAndMarkerPair(number);
        if (pair != null) {
            MarkerInfo markerInfo = pair.getValue();
            markerInfo.marker.remove();
            markerIconBuilder.release(markerInfo.icon);
            markers.remove(pair.getKey());
        }
    }

    private void removeVehiclesByTransportAndRoute(int transportId, int routeNumber) {
        Iterator<Map.Entry<MapVehicle, MarkerInfo>> iterator = markers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<MapVehicle, MarkerInfo> pair = iterator.next();
            MapVehicle vehicle = pair.getKey();
            if (vehicle.getTransportId() == transportId && vehicle.getRouteNumber() == routeNumber) {
                MarkerInfo markerInfo = pair.getValue();
                markerInfo.marker.remove();
                markerIconBuilder.release(markerInfo.icon);
                iterator.remove();
            }
        }
    }

    private void removeAllVehicles() {
        for (MarkerInfo markerInfo: markers.values()) {
            markerInfo.marker.remove();
            markerIconBuilder.release(markerInfo.icon);
        }
        markers.clear();
    }

    private Map.Entry<MapVehicle, MarkerInfo> getMapVehicleAndMarkerPair(String number) {
        for (Map.Entry<MapVehicle, MarkerInfo> item: markers.entrySet()) {
            MapVehicle vehicle = item.getKey();
            if (vehicle.getNumber().equals(number)) {
                return item;
            }
        }
        return null;
    }

    private LatLng getGeoPoint(MapVehicle vehicle) {
        return new LatLng(vehicle.getLatitude().doubleValue(), vehicle.getLongitude().doubleValue());
    }

    private void requestForData() {
        EventManager manager = App.get().getEventManager();
        manager.publish(new RequestLoadServiceEvent());
        manager.publish(new RequestLoadRoutesEvent());
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        App.get().getEventManager().unsubscribeAll(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
