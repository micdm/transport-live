package com.micdm.transportlive.activities;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.micdm.transportlive.App;
import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.ForecastVehicle;
import com.micdm.transportlive.data.MapVehicle;
import com.micdm.transportlive.data.SelectedRoute;
import com.micdm.transportlive.data.SelectedStation;
import com.micdm.transportlive.data.service.Service;
import com.micdm.transportlive.events.EventManager;
import com.micdm.transportlive.events.EventType;
import com.micdm.transportlive.events.events.LoadRoutesEvent;
import com.micdm.transportlive.events.events.LoadServiceEvent;
import com.micdm.transportlive.events.events.LoadStationsEvent;
import com.micdm.transportlive.events.events.RemoveAllDataEvent;
import com.micdm.transportlive.events.events.RemoveVehicleEvent;
import com.micdm.transportlive.events.events.RequestLoadRoutesEvent;
import com.micdm.transportlive.events.events.RequestLoadServiceEvent;
import com.micdm.transportlive.events.events.RequestLoadStationsEvent;
import com.micdm.transportlive.events.events.RequestSelectRouteEvent;
import com.micdm.transportlive.events.events.RequestSelectStationEvent;
import com.micdm.transportlive.events.events.RequestUnselectRouteEvent;
import com.micdm.transportlive.events.events.RequestUnselectStationEvent;
import com.micdm.transportlive.events.events.UnselectRouteEvent;
import com.micdm.transportlive.events.events.UpdateForecastEvent;
import com.micdm.transportlive.events.events.UpdateVehicleEvent;
import com.micdm.transportlive.fragments.ForecastFragment;
import com.micdm.transportlive.fragments.MapFragment;
import com.micdm.transportlive.misc.ServiceLoader;
import com.micdm.transportlive.misc.Utils;
import com.micdm.transportlive.misc.ViewPager;
import com.micdm.transportlive.misc.analytics.Analytics;
import com.micdm.transportlive.server.ServerGate;
import com.micdm.transportlive.server.messages.Message;
import com.micdm.transportlive.server.messages.incoming.ForecastMessage;
import com.micdm.transportlive.server.messages.incoming.VehicleMessage;
import com.micdm.transportlive.stores.SelectedRouteStore;
import com.micdm.transportlive.stores.SelectedStationStore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
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
        
        private final List<Page> pages = new ArrayList<Page>();

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
        final SelectedStationStore selectedStationStore = app.getSelectedStationStore();
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
                gate.selectRoute(route);
                manager.publish(new LoadRoutesEvent(selectedRoutes));
            }
        });
        manager.subscribe(this, EventType.REQUEST_UNSELECT_ROUTE, new EventManager.OnEventListener<RequestUnselectRouteEvent>() {
            @Override
            public void onEvent(RequestUnselectRouteEvent event) {
                SelectedRoute route = event.getRoute();
                int transportId = route.getTransportId();
                int routeNumber = route.getRouteNumber();
                removeSelectedRoute(transportId, routeNumber);
                selectedRouteStore.put(selectedRoutes);
                gate.unselectRoute(route);
                manager.publish(new UnselectRouteEvent(route));
                manager.publish(new LoadRoutesEvent(selectedRoutes));
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
                selectedStationStore.put(selectedStations);
                gate.selectStation(station);
                manager.publish(new LoadStationsEvent(selectedStations));
            }
        });
        manager.subscribe(this, EventType.REQUEST_UNSELECT_STATION, new EventManager.OnEventListener<RequestUnselectStationEvent>() {
            @Override
            public void onEvent(RequestUnselectStationEvent event) {
                SelectedStation station = event.getStation();
                removeSelectedStation(station.getTransportId(), station.getStationId());
                selectedStationStore.put(selectedStations);
                gate.unselectStation(station);
                manager.publish(new LoadStationsEvent(selectedStations));
            }
        });
    }

    private void removeSelectedRoute(int transportId, int routeNumber) {
        Iterator<SelectedRoute> iterator = selectedRoutes.iterator();
        while (iterator.hasNext()) {
            SelectedRoute route = iterator.next();
            if (route.getTransportId() == transportId && route.getRouteNumber() == routeNumber) {
                iterator.remove();
            }
        }
    }

    private void removeSelectedStation(int transportId, int stationId) {
        Iterator<SelectedStation> iterator = selectedStations.iterator();
        while (iterator.hasNext()) {
            SelectedStation station = iterator.next();
            if (station.getTransportId() == transportId && station.getStationId() == stationId) {
                iterator.remove();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        App.get().getAnalytics().reportActivityStart(this);
        subscribeForData();
    }

    private void subscribeForData() {
        final ServerGate gate = App.get().getServerGate();
        final Drawable connectingIcon = getConnectingIcon();
        gate.connect(new ServerGate.OnConnectListener() {
            @Override
            public void onStartConnect(int tryNumber) {
                ActionBar actionBar = getActionBar();
                actionBar.setIcon(connectingIcon);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.__connecting));
                if (tryNumber > 1) {
                    showNoConnectionNotice();
                    App.get().getAnalytics().reportEvent(Analytics.Category.MISC, Analytics.Action.MISC, "no_connection", tryNumber);
                }
            }
            @Override
            public void OnCompleteConnect() {
                ActionBar actionBar = getActionBar();
                actionBar.setIcon(R.drawable.ic_launcher);
                actionBar.setDisplayShowTitleEnabled(false);
                App.get().getEventManager().publish(new RemoveAllDataEvent());
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
            }
        });
    }

    private Drawable getConnectingIcon() {
        Drawable original = getResources().getDrawable(R.drawable.ic_launcher);
        Drawable icon = original.getConstantState().newDrawable().mutate();
        icon.setColorFilter(getResources().getColor(R.color.connecting_icon), PorterDuff.Mode.SRC_ATOP);
        return icon;
    }

    private void showNoConnectionNotice() {
        Toast.makeText(this, getString(R.string.__no_connection_after_several_tries), Toast.LENGTH_LONG).show();
    }

    private void handleVehicleMessage(VehicleMessage message) {
        String number = message.getNumber();
        BigDecimal latitude = message.getLatitude();
        BigDecimal longitude = message.getLongitude();
        EventManager manager = App.get().getEventManager();
        if (latitude.equals(BigDecimal.ZERO) && longitude.equals(BigDecimal.ZERO)) {
            manager.publish(new RemoveVehicleEvent(number));
        } else {
            MapVehicle vehicle = new MapVehicle(number, message.getTransportId(), message.getRouteNumber(), latitude, longitude, message.getCourse());
            manager.publish(new UpdateVehicleEvent(vehicle));
        }
    }

    private void handleForecastMessage(ForecastMessage message) {
        int transportId = message.getTransportId();
        int stationId = message.getStationId();
        List<ForecastVehicle> vehicles = new ArrayList<ForecastVehicle>();
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

    @Override
    protected void onStop() {
        super.onStop();
        App.get().getServerGate().disconnect();
        App.get().getAnalytics().reportActivityStop(this);
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
            case R.id.m__main__settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.get().getEventManager().unsubscribeAll(this);
    }
}
