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
import com.micdm.transportlive.data.VehicleInfo;
import com.micdm.transportlive.misc.AssetArchive;
import com.micdm.transportlive.misc.ServiceHandler;

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
                markers.add(getMarker(info));
            }
            return markers;
        }

        private OverlayItem getMarker(VehicleInfo info) {
            OverlayItem marker = new OverlayItem(info.vehicle.number, "", new GeoPoint(info.vehicle.latitude, info.vehicle.longitude));
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

    private static final int TILE_SIZE = 256;
    private static final int MIN_ZOOM = 14;
    private static final int MAX_ZOOM = 15;
    private static final int NORTH_EDGE = 56547372;
    private static final int WEST_EDGE = 84902344;
    private static final int SOUTH_EDGE = 56438204;
    private static final int EAST_EDGE = 85122070;
    private static final GeoPoint INITIAL_LOCATION = new GeoPoint(56484642, 84948100);

    private ServiceHandler serviceHandler;
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
            View reconnectView = view.findViewById(R.id.reconnect);
            reconnectView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    serviceHandler.loadVehicles();
                }
            });
            MapView mapView = getMapView();
            ((ViewGroup) view).addView(mapView, 0);
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
        IRegisterReceiver receiver = new SimpleRegisterReceiver(getActivity());
        ITileSource source = new XYTileSource("OSMPublicTransport", ResourceProxy.string.public_transport, MIN_ZOOM, MAX_ZOOM, TILE_SIZE, ".png", null);
        AssetArchive archive = AssetArchive.getAssetArchive(getActivity());
        MapTileFileArchiveProvider provider = new MapTileFileArchiveProvider(receiver, source, new IArchiveFile[] {archive});
        return new MapTileProviderArray(source, receiver, new MapTileModuleProviderBase[] {provider});
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        hideAllViews();
        serviceHandler.setOnUnselectAllRoutesListener(new ServiceHandler.OnUnselectAllRoutesListener() {
            @Override
            public void onUnselectAllRoutes() {
                hideAllViews();
                showView(R.id.no_route_selected);
            }
        });
        serviceHandler.setOnLoadVehiclesListener(new ServiceHandler.OnLoadVehiclesListener() {
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
            @Override
            public void onError() {
                hideAllViews();
                showView(R.id.no_connection);
            }
        });
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

    private void showView(int id) {
        getView().findViewById(id).setVisibility(View.VISIBLE);
    }

    private void hideView(int id) {
        getView().findViewById(id).setVisibility(View.GONE);
    }
    
    private void hideAllViews() {
        hideView(R.id.map);
        hideView(R.id.no_route_selected);
        hideView(R.id.no_connection);
        hideView(R.id.no_vehicles);
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
    public void onDestroy() {
        super.onDestroy();
        serviceHandler.setOnUnselectAllRoutesListener(null);
        serviceHandler.setOnLoadVehiclesListener(null);
    }
}
