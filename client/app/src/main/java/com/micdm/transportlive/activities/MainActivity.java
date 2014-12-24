package com.micdm.transportlive.activities;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.micdm.transportlive.App;
import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.ForecastVehicle;
import com.micdm.transportlive.data.MapVehicle;
import com.micdm.transportlive.data.SelectedRoute;
import com.micdm.transportlive.data.SelectedStation;
import com.micdm.transportlive.data.service.Direction;
import com.micdm.transportlive.data.service.Route;
import com.micdm.transportlive.data.service.Service;
import com.micdm.transportlive.data.service.Station;
import com.micdm.transportlive.data.service.Transport;
import com.micdm.transportlive.events.EventManager;
import com.micdm.transportlive.events.EventType;
import com.micdm.transportlive.events.events.LoadRoutesEvent;
import com.micdm.transportlive.events.events.LoadServiceEvent;
import com.micdm.transportlive.events.events.LoadStationsEvent;
import com.micdm.transportlive.events.events.RemoveAllDataEvent;
import com.micdm.transportlive.events.events.RemoveVehicleEvent;
import com.micdm.transportlive.events.events.RequestFavouriteStationEvent;
import com.micdm.transportlive.events.events.RequestFocusVehicleEvent;
import com.micdm.transportlive.events.events.RequestLoadNearestStationsEvent;
import com.micdm.transportlive.events.events.RequestLoadRoutesEvent;
import com.micdm.transportlive.events.events.RequestLoadServiceEvent;
import com.micdm.transportlive.events.events.RequestLoadStationsEvent;
import com.micdm.transportlive.events.events.RequestSelectRouteEvent;
import com.micdm.transportlive.events.events.RequestSelectStationEvent;
import com.micdm.transportlive.events.events.RequestUnfavouriteStationEvent;
import com.micdm.transportlive.events.events.RequestUnselectRouteEvent;
import com.micdm.transportlive.events.events.RequestUnselectStationEvent;
import com.micdm.transportlive.events.events.UnselectRouteEvent;
import com.micdm.transportlive.events.events.UpdateForecastEvent;
import com.micdm.transportlive.events.events.UpdateLocationEvent;
import com.micdm.transportlive.events.events.UpdateNearestStationsEvent;
import com.micdm.transportlive.events.events.UpdateVehicleEvent;
import com.micdm.transportlive.fragments.ForecastFragment;
import com.micdm.transportlive.fragments.FragmentTag;
import com.micdm.transportlive.fragments.MapFragment;
import com.micdm.transportlive.fragments.SelectRouteFragment;
import com.micdm.transportlive.fragments.SelectStationFragment;
import com.micdm.transportlive.location.DefaultLocator;
import com.micdm.transportlive.location.Locator;
import com.micdm.transportlive.misc.ServiceLoader;
import com.micdm.transportlive.misc.Utils;
import com.micdm.transportlive.misc.ViewPager;
import com.micdm.transportlive.misc.analytics.Analytics;
import com.micdm.transportlive.server.ServerGate;
import com.micdm.transportlive.server.messages.Message;
import com.micdm.transportlive.server.messages.incoming.ForecastMessage;
import com.micdm.transportlive.server.messages.incoming.NearestStationsMessage;
import com.micdm.transportlive.server.messages.incoming.VehicleMessage;
import com.micdm.transportlive.stores.SelectedRouteStore;
import com.micdm.transportlive.stores.SelectedStationStore;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {

    private static class CustomPagerAdapter extends FragmentPagerAdapter {

        public static class Page {

            public final String title;
            public final Fragment fragment;

            public Page(String title, Fragment fragment) {
                this.title = title;
                this.fragment = fragment;
            }
        }
        
        private final List<Page> pages = new ArrayList<>();

        public CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void add(Page page) {
            pages.add(page);
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int i) {
            return pages.get(i).fragment;
        }

        @Override
        public int getCount() {
            return pages.size();
        }

        @Override
        public CharSequence getPageTitle(int i) {
            return pages.get(i).title;
        }
    }

    private static final int MAP_TAB_INDEX = 0;
    private static final int FORECAST_TAB_INDEX = 1;

    private Service service;

    private List<SelectedRoute> selectedRoutes;
    private List<SelectedStation> selectedStations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a__main);
        setupActionBar();
        setupPager();
        subscribeForEvents();
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }

    private void setupPager() {
        ViewPager pager = (ViewPager) findViewById(R.id.a__main__pager);
        pager.setAdapter(new CustomPagerAdapter(getSupportFragmentManager()));
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int i) {
                getActionBar().setSelectedNavigationItem(i);
                App.get().getAnalytics().reportEvent(Analytics.Category.TABS, Analytics.Action.SHOW, String.valueOf(i));
            }
        });
        addPage(pager, new CustomPagerAdapter.Page(getString(R.string.__tab_title_map), new MapFragment()));
        addPage(pager, new CustomPagerAdapter.Page(getString(R.string.__tab_title_forecast), new ForecastFragment()));
    }

    private void addPage(final ViewPager pager, CustomPagerAdapter.Page page) {
        ((CustomPagerAdapter) pager.getAdapter()).add(page);
        ActionBar actionBar = getActionBar();
        ActionBar.Tab tab = actionBar.newTab();
        tab.setText(page.title);
        tab.setTabListener(new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                pager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        });
        actionBar.addTab(tab);
    }

    private void subscribeForEvents() {
        App app = App.get();
        final EventManager manager = app.getEventManager();
        final ServerGate gate = app.getServerGate();
        final ServiceLoader serviceLoader = app.getServiceLoader();
        final SelectedRouteStore selectedRouteStore = app.getSelectedRouteStore();
        manager.subscribe(this, EventType.REQUEST_LOAD_SERVICE, new EventManager.OnEventListener<RequestLoadServiceEvent>() {
            @Override
            public void onEvent(RequestLoadServiceEvent event) {
                if (service == null) {
                    service = serviceLoader.load();
                }
                manager.publish(new LoadServiceEvent(service));
            }
        });
        manager.subscribe(this, EventType.REQUEST_LOAD_ROUTES, new EventManager.OnEventListener<RequestLoadRoutesEvent>() {
            @Override
            public void onEvent(RequestLoadRoutesEvent event) {
                if (selectedRoutes == null) {
                    selectedRoutes = (new SelectedRouteStore(MainActivity.this)).load(service);
                    for (SelectedRoute route: selectedRoutes) {
                        gate.selectRoute(route);
                    }
                }
                manager.publish(new LoadRoutesEvent(selectedRoutes));
            }
        });
        manager.subscribe(this, EventType.REQUEST_SELECT_ROUTE, new EventManager.OnEventListener<RequestSelectRouteEvent>() {
            @Override
            public void onEvent(RequestSelectRouteEvent event) {
                SelectedRoute route = event.getRoute();
                if (Utils.isRouteSelected(selectedRoutes, route.getTransportId(), route.getRouteNumber())) {
                    return;
                }
                selectedRoutes.add(route);
                selectedRouteStore.put(selectedRoutes);
                manager.publish(new LoadRoutesEvent(selectedRoutes));
                gate.selectRoute(route);
            }
        });
        manager.subscribe(this, EventType.REQUEST_UNSELECT_ROUTE, new EventManager.OnEventListener<RequestUnselectRouteEvent>() {
            @Override
            public void onEvent(RequestUnselectRouteEvent event) {
                SelectedRoute route = event.getRoute();
                int transportId = route.getTransportId();
                int routeNumber = route.getRouteNumber();
                if (!Utils.isRouteSelected(selectedRoutes, transportId, routeNumber)) {
                    return;
                }
                selectedRoutes.remove(getSelectedRoute(transportId, routeNumber));
                selectedRouteStore.put(selectedRoutes);
                manager.publish(new UnselectRouteEvent(route));
                manager.publish(new LoadRoutesEvent(selectedRoutes));
                gate.unselectRoute(route);
            }
        });
        manager.subscribe(this, EventType.REQUEST_LOAD_STATIONS, new EventManager.OnEventListener<RequestLoadStationsEvent>() {
            @Override
            public void onEvent(RequestLoadStationsEvent event) {
                if (selectedStations == null) {
                    selectedStations = (new SelectedStationStore(MainActivity.this)).load(service);
                    for (SelectedStation station: selectedStations) {
                        gate.selectStation(station);
                    }
                }
                manager.publish(new LoadStationsEvent(selectedStations));
            }
        });
        manager.subscribe(this, EventType.REQUEST_SELECT_STATION, new EventManager.OnEventListener<RequestSelectStationEvent>() {
            @Override
            public void onEvent(RequestSelectStationEvent event) {
                SelectedStation station = event.getStation();
                if (Utils.isStationSelected(selectedStations, station.getTransportId(), station.getStationId())) {
                    return;
                }
                selectedStations.add(station);
                manager.publish(new LoadStationsEvent(selectedStations));
                gate.selectStation(station);
            }
        });
        manager.subscribe(this, EventType.REQUEST_UNSELECT_STATION, new EventManager.OnEventListener<RequestUnselectStationEvent>() {
            @Override
            public void onEvent(RequestUnselectStationEvent event) {
                SelectedStation station = event.getStation();
                int transportId = station.getTransportId();
                int stationId = station.getStationId();
                if (!Utils.isStationSelected(selectedStations, transportId, stationId)) {
                    return;
                }
                selectedStations.remove(getSelectedStation(transportId, stationId));
                manager.publish(new LoadStationsEvent(selectedStations));
                gate.unselectStation(station);
            }
        });
        manager.subscribe(this, EventType.REQUEST_FAVOURITE_STATION, new EventManager.OnEventListener<RequestFavouriteStationEvent>() {
            @Override
            public void onEvent(RequestFavouriteStationEvent event) {
                setStationFavourite(event.getStation(), true);
            }
        });
        manager.subscribe(this, EventType.REQUEST_UNFAVOURITE_STATION, new EventManager.OnEventListener<RequestUnfavouriteStationEvent>() {
            @Override
            public void onEvent(RequestUnfavouriteStationEvent event) {
                setStationFavourite(event.getStation(), false);
            }
        });
        manager.subscribe(this, EventType.REQUEST_FOCUS_VEHICLE, new EventManager.OnEventListener<RequestFocusVehicleEvent>() {
            @Override
            public void onEvent(RequestFocusVehicleEvent event) {
                getActionBar().setSelectedNavigationItem(MAP_TAB_INDEX);
            }
        });
        manager.subscribe(this, EventType.REQUEST_LOAD_NEAREST_STATIONS, new EventManager.OnEventListener<RequestLoadNearestStationsEvent>() {
            @Override
            public void onEvent(RequestLoadNearestStationsEvent event) {
                gate.loadNearestStations(event.getLatitude(), event.getLongitude());
            }
        });
    }

    private SelectedRoute getSelectedRoute(int transportId, int routeNumber) {
        for (SelectedRoute route: selectedRoutes) {
            if (route.getTransportId() == transportId && route.getRouteNumber() == routeNumber) {
                return route;
            }
        }
        throw new RuntimeException(String.format("cannot find route %s %s", transportId, routeNumber));
    }

    private SelectedStation getSelectedStation(int transportId, int stationId) {
        for (SelectedStation station: selectedStations) {
            if (station.getTransportId() == transportId && station.getStationId() == stationId) {
                return station;
            }
        }
        throw new RuntimeException(String.format("cannot find station %s %s", transportId, stationId));
    }

    private void setStationFavourite(SelectedStation station, boolean value) {
        int transportId = station.getTransportId();
        int stationId = station.getStationId();
        if (!Utils.isStationSelected(selectedStations, transportId, stationId)) {
            return;
        }
        selectedStations.remove(getSelectedStation(transportId, stationId));
        selectedStations.add(new SelectedStation(transportId, station.getRouteNumber(), station.getDirectionId(), stationId, value));
        App app = App.get();
        app.getSelectedStationStore().put(getFavouriteStations());
        app.getEventManager().publish(new LoadStationsEvent(selectedStations));
    }

    private List<SelectedStation> getFavouriteStations() {
        List<SelectedStation> favourite = new ArrayList<>();
        for (SelectedStation station: selectedStations) {
            if (station.isFavourite()) {
                favourite.add(station);
            }
        }
        return favourite;
    }

    @Override
    protected void onStart() {
        super.onStart();
        App.get().getAnalytics().reportActivityStart(this);
        subscribeForData();
        subscribeForLocation();
    }

    private void subscribeForData() {
        App app = App.get();
        final ServerGate gate = app.getServerGate();
        final EventManager manager = app.getEventManager();
        final Drawable connectingIcon = getConnectingIcon();
        gate.connect(new ServerGate.OnConnectListener() {
            @Override
            public void onStartConnect(int tryNumber) {
                ActionBar actionBar = getActionBar();
                actionBar.setIcon(connectingIcon);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.__connecting));
                if (tryNumber > 1) {
                    App.get().getAnalytics().reportEvent(Analytics.Category.MISC, Analytics.Action.MISC, "no_connection", tryNumber);
                }
            }
            @Override
            public void OnCompleteConnect() {
                ActionBar actionBar = getActionBar();
                actionBar.setIcon(R.drawable.ic_launcher);
                actionBar.setDisplayShowTitleEnabled(false);
                manager.publish(new RemoveAllDataEvent());
                if (selectedRoutes != null) {
                    for (SelectedRoute route: selectedRoutes) {
                        gate.selectRoute(route);
                    }
                }
                if (selectedStations != null) {
                    for (SelectedStation station: selectedStations) {
                        gate.selectStation(station);
                    }
                }
            }
        }, new ServerGate.OnMessageListener() {
            @Override
            public void onMessage(Message message) {
                if (message instanceof VehicleMessage) {
                    handleVehicleMessage((VehicleMessage) message);
                }
                if (message instanceof ForecastMessage) {
                    handleForecastMessage((ForecastMessage) message);
                }
                if (message instanceof NearestStationsMessage) {
                    handleNearestStationsMessage((NearestStationsMessage) message);
                }
            }
        });
    }

    private Drawable getConnectingIcon() {
        Drawable original = getResources().getDrawable(R.drawable.ic_launcher);
        Drawable icon = original.getConstantState().newDrawable().mutate();
        icon.setColorFilter(getResources().getColor(R.color.connecting_icon), PorterDuff.Mode.SRC_ATOP);
        return icon;
    }

    private void handleVehicleMessage(VehicleMessage message) {
        int transportId = message.getTransportId();
        int routeNumber = message.getRouteNumber();
        if (!Utils.isRouteSelected(selectedRoutes, transportId, routeNumber)) {
            return;
        }
        EventManager manager = App.get().getEventManager();
        String number = message.getNumber();
        BigDecimal latitude = message.getLatitude();
        BigDecimal longitude = message.getLongitude();
        if (latitude.equals(BigDecimal.ZERO) && longitude.equals(BigDecimal.ZERO)) {
            manager.publish(new RemoveVehicleEvent(number));
        } else {
            MapVehicle vehicle = new MapVehicle(number, transportId, routeNumber, latitude, longitude, message.getCourse());
            manager.publish(new UpdateVehicleEvent(vehicle));
        }
    }

    private void handleForecastMessage(ForecastMessage message) {
        int transportId = message.getTransportId();
        int stationId = message.getStationId();
        if (!Utils.isStationSelected(selectedStations, transportId, stationId)) {
            return;
        }
        List<ForecastVehicle> vehicles = new ArrayList<>();
        for (ForecastMessage.Vehicle vehicle: message.getVehicles()) {
            String number = vehicle.getNumber();
            int routeNumber = vehicle.getRouteNumber();
            int arrivalTime = vehicle.getArrivalTime();
            boolean isLowFloor = vehicle.isLowFloor();
            vehicles.add(new ForecastVehicle(number, routeNumber, arrivalTime, isLowFloor));
        }
        Forecast forecast = new Forecast(transportId, stationId, vehicles);
        App.get().getEventManager().publish(new UpdateForecastEvent(forecast));
    }

    private void handleNearestStationsMessage(NearestStationsMessage message) {
        List<SelectedStation> stations = new ArrayList<>();
        for (NearestStationsMessage.Station station: message.getStations()) {
            stations.add(getFirstSuitableStation(station.getTransportId(), station.getStationId()));
        }
        App.get().getEventManager().publish(new UpdateNearestStationsEvent(stations));
    }

    private SelectedStation getFirstSuitableStation(int transportId, int stationId) {
        Transport transport = service.getTransportById(transportId);
        for (Route route: transport.getRoutes()) {
            for (Direction direction: route.getDirections()) {
                for (Station station: direction.getStations()) {
                    if (station.getId() == stationId) {
                        return new SelectedStation(transportId, route.getNumber(), direction.getId(), stationId);
                    }
                }
            }
        }
        throw new RuntimeException(String.format("cannot find station %s %s", transportId, stationId));
    }

    private void subscribeForLocation() {
        Locator locator = App.get().getLocator();
        locator.setOnUpdateLocationListener(new DefaultLocator.OnUpdateLocationListener() {
            @Override
            public void onUpdateLocation(Location location) {
                BigDecimal latitude = new BigDecimal(location.getLatitude(), MathContext.DECIMAL64);
                BigDecimal longitude = new BigDecimal(location.getLongitude(), MathContext.DECIMAL64);
                App.get().getEventManager().publish(new UpdateLocationEvent(latitude, longitude));
            }
        });
        locator.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        App app = App.get();
        app.getLocator().stop();
        app.getServerGate().disconnect();
        app.getAnalytics().reportActivityStop(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m__main__add:
                switch (getActionBar().getSelectedNavigationIndex()) {
                    case MAP_TAB_INDEX:
                        showSelectRouteFragment();
                        break;
                    case FORECAST_TAB_INDEX:
                        showSelectStationFragment();
                        break;
                }
                return true;
            case R.id.m__main__settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSelectRouteFragment() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentByTag(FragmentTag.SELECT_ROUTE) == null) {
            (new SelectRouteFragment()).show(manager, FragmentTag.SELECT_ROUTE);
            App.get().getAnalytics().reportEvent(Analytics.Category.DIALOGS, Analytics.Action.SHOW, "select_route");
        }
    }

    private void showSelectStationFragment() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentByTag(FragmentTag.SELECT_STATION) == null) {
            (new SelectStationFragment()).show(manager, FragmentTag.SELECT_STATION);
            App.get().getAnalytics().reportEvent(Analytics.Category.DIALOGS, Analytics.Action.SHOW, "select_station");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.get().getEventManager().unsubscribeAll(this);
    }
}
