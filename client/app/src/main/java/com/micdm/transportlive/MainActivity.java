package com.micdm.transportlive;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.preference.PreferenceFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.RouteInfo;
import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Station;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.donate.DonateItem;
import com.micdm.transportlive.donate.DonateManager;
import com.micdm.transportlive.fragments.AboutFragment;
import com.micdm.transportlive.fragments.DonateFragment;
import com.micdm.transportlive.fragments.ForecastFragment;
import com.micdm.transportlive.fragments.MapFragment;
import com.micdm.transportlive.fragments.NoConnectionFragment;
import com.micdm.transportlive.fragments.SelectRouteFragment;
import com.micdm.transportlive.fragments.SelectStationFragment;
import com.micdm.transportlive.fragments.SettingsFragment;
import com.micdm.transportlive.interfaces.ConnectionHandler;
import com.micdm.transportlive.interfaces.DonateHandler;
import com.micdm.transportlive.interfaces.EventListener;
import com.micdm.transportlive.interfaces.ForecastHandler;
import com.micdm.transportlive.interfaces.ServiceHandler;
import com.micdm.transportlive.misc.EventListenerManager;
import com.micdm.transportlive.misc.ServiceLoader;
import com.micdm.transportlive.server.pollers.ForecastPoller;
import com.micdm.transportlive.server.pollers.VehiclePoller;
import com.micdm.transportlive.stores.SelectedRouteStore;
import com.micdm.transportlive.stores.SelectedStationStore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// TODO: добавить очистку настроек и файлов-хранилищ при обновлении на новую версию
public class MainActivity extends ActionBarActivity implements PreferenceFragment.OnPreferenceStartFragmentCallback,
        ConnectionHandler, ServiceHandler, ForecastHandler, DonateHandler {

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

    private static final String FRAGMENT_ABOUT_TAG = "about";
    private static final String FRAGMENT_NO_CONNECTION_TAG = "no_connection";
    private static final String FRAGMENT_SELECT_STATION_TAG = "select_station";
    private static final String FRAGMENT_SELECT_ROUTE_TAG = "select_route";
    private static final String FRAGMENT_DONATE_TAG = "donate";

    private static final String EVENT_LISTENER_KEY_ON_UNSELECT_ALL_ROUTES = "OnUnselectAllRoutes";
    private static final String EVENT_LISTENER_KEY_ON_LOAD_SERVICE = "OnLoadService";
    private static final String EVENT_LISTENER_KEY_ON_LOAD_VEHICLES = "OnLoadVehicles";
    private static final String EVENT_LISTENER_KEY_ON_LOAD_STATIONS = "OnLoadStations";
    private static final String EVENT_LISTENER_KEY_ON_SELECT_STATION = "OnSelectStation";
    private static final String EVENT_LISTENER_KEY_ON_UNSELECT_STATION = "OnUnselectStation";
    private static final String EVENT_LISTENER_KEY_ON_UNSELECT_ALL_STATIONS = "OnUnselectAllStations";
    private static final String EVENT_LISTENER_KEY_ON_LOAD_FORECASTS = "OnLoadForecasts";
    private static final String EVENT_LISTENER_KEY_ON_LOAD_DONATE_ITEMS = "OnLoadDonateItems";

    private final EventListenerManager listeners = new EventListenerManager();

    private final VehiclePoller vehiclePoller = new VehiclePoller(this, new VehiclePoller.OnLoadListener() {
        @Override
        public void onStart() {
            listeners.notify(EVENT_LISTENER_KEY_ON_LOAD_VEHICLES, new EventListenerManager.OnIterateListener() {
                @Override
                public void onIterate(EventListener listener) {
                    ((OnLoadVehiclesListener) listener).onStart();
                }
            });
        }
        @Override
        public void onFinish() {
            listeners.notify(EVENT_LISTENER_KEY_ON_LOAD_VEHICLES, new EventListenerManager.OnIterateListener() {
                @Override
                public void onIterate(EventListener listener) {
                    ((OnLoadVehiclesListener) listener).onFinish();
                }
            });
        }
        @Override
        public void onLoad(final List<RouteInfo> loaded) {
            hideNoConnectionMessage();
            vehicles = loaded;
            listeners.notify(EVENT_LISTENER_KEY_ON_LOAD_VEHICLES, new EventListenerManager.OnIterateListener() {
                @Override
                public void onIterate(EventListener listener) {
                    ((OnLoadVehiclesListener) listener).onLoadVehicles(loaded);
                }
            });
        }
        @Override
        public void onError() {
            vehiclePoller.stop();
            forecastPoller.stop();
            showNoConnectionMessage();
        }
    });
    private Service service;
    private List<SelectedRouteInfo> selectedRoutes;
    private List<RouteInfo> vehicles;

    private final ForecastPoller forecastPoller = new ForecastPoller(this, new ForecastPoller.OnLoadListener() {
        @Override
        public void onStart() {
            listeners.notify(EVENT_LISTENER_KEY_ON_LOAD_FORECASTS, new EventListenerManager.OnIterateListener() {
                @Override
                public void onIterate(EventListener listener) {
                    ((OnLoadForecastsListener) listener).onStart();
                }
            });
        }
        @Override
        public void onFinish() {
            listeners.notify(EVENT_LISTENER_KEY_ON_LOAD_FORECASTS, new EventListenerManager.OnIterateListener() {
                @Override
                public void onIterate(EventListener listener) {
                    ((OnLoadForecastsListener) listener).onFinish();
                }
            });
        }
        @Override
        public void onLoad(final List<Forecast> loaded) {
            hideNoConnectionMessage();
            forecasts = loaded;
            listeners.notify(EVENT_LISTENER_KEY_ON_LOAD_FORECASTS, new EventListenerManager.OnIterateListener() {
                @Override
                public void onIterate(EventListener listener) {
                    ((OnLoadForecastsListener) listener).onLoadForecasts(forecasts);
                }
            });
        }
        @Override
        public void onError() {
            vehiclePoller.stop();
            forecastPoller.stop();
            showNoConnectionMessage();
        }
    });
    private List<SelectedStationInfo> selectedStations;
    private List<Forecast> forecasts;

    private DonateManager donateManager = new DonateManager(this, new DonateManager.OnLoadItemsListener() {
        @Override
        public void onLoadItems(final List<DonateItem> items) {
            listeners.notify(EVENT_LISTENER_KEY_ON_LOAD_DONATE_ITEMS, new EventListenerManager.OnIterateListener() {
                @Override
                public void onIterate(EventListener listener) {
                    ((OnLoadDonateItemsListener) listener).onLoadDonateItems(items);
                }
            });
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CustomApplication) getApplication()).getTracker();
        donateManager.init();
        setContentView(R.layout.a__main);
        setupActionBar();
        setupPager();
        loadData();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }

    private void setupPager() {
        CustomViewPager pager = (CustomViewPager) findViewById(R.id.a__main__pager);
        pager.setAdapter(new CustomPagerAdapter(getSupportFragmentManager()));
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int i) {
                getSupportActionBar().setSelectedNavigationItem(i);
            }
        });
        addPage(pager, new CustomPagerAdapter.Page(getString(R.string.tab_title_forecast), new ForecastFragment()));
        addPage(pager, new CustomPagerAdapter.Page(getString(R.string.tab_title_map), new MapFragment()));
        addPage(pager, new CustomPagerAdapter.Page(getString(R.string.tab_title_settings), new SettingsFragment()));
    }

    private void addPage(final CustomViewPager pager, CustomPagerAdapter.Page page) {
        ((CustomPagerAdapter) pager.getAdapter()).add(page);
        ActionBar actionBar = getSupportActionBar();
        ActionBar.Tab tab = actionBar.newTab();
        tab.setText(page.title);
        tab.setTabListener(new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                pager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}
            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}
        });
        actionBar.addTab(tab);
    }

    private void loadData() {
        service = (new ServiceLoader(this)).load();
        selectedRoutes = (new SelectedRouteStore(this)).load(service);
        if (selectedRoutes.isEmpty()) {
            listeners.notify(EVENT_LISTENER_KEY_ON_UNSELECT_ALL_ROUTES, new EventListenerManager.OnIterateListener() {
                @Override
                public void onIterate(EventListener listener) {
                    ((OnUnselectAllRoutesListener) listener).onUnselectAllRoutes();
                }
            });
        }
        listeners.notify(EVENT_LISTENER_KEY_ON_LOAD_SERVICE, new EventListenerManager.OnIterateListener() {
            @Override
            public void onIterate(EventListener listener) {
                ((OnLoadServiceListener) listener).onLoadService(service);
            }
        });
        selectedStations = (new SelectedStationStore(this)).load(service);
        listeners.notify(EVENT_LISTENER_KEY_ON_LOAD_STATIONS, new EventListenerManager.OnIterateListener() {
            @Override
            public void onIterate(EventListener listener) {
                ((OnLoadStationsListener) listener).onLoadStations(selectedStations);
            }
        });
        if (selectedStations.isEmpty()) {
            listeners.notify(EVENT_LISTENER_KEY_ON_UNSELECT_ALL_STATIONS, new EventListenerManager.OnIterateListener() {
                @Override
                public void onIterate(EventListener listener) {
                    ((OnUnselectAllStationsListener) listener).onUnselectAllStations();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        loadVehicles();
        loadForecasts();
    }

    @Override
    protected void onStop() {
        super.onStop();
        vehiclePoller.stop();
        forecastPoller.stop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        donateManager.deinit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.common, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reload:
                loadVehicles();
                loadForecasts();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment fragment, Preference pref) {
        String key = pref.getKey();
        if (key == null) {
            return false;
        }
        if (key.equals(SettingsFragment.PREF_KEY_DONATE)) {
            showDonateMessage();
            return true;
        }
        if (key.equals(SettingsFragment.PREF_KEY_SHARE)) {
            showShareMessage();
            return true;
        }
        if (key.equals(SettingsFragment.PREF_KEY_ABOUT)) {
            showAboutMessage();
            return true;
        }
        return false;
    }

    private void showDonateMessage() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentByTag(FRAGMENT_DONATE_TAG) == null) {
            (new DonateFragment()).show(manager, FRAGMENT_DONATE_TAG);
        }
    }

    private void showShareMessage() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text, getPackageName()));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {}
    }

    private void showAboutMessage() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentByTag(FRAGMENT_ABOUT_TAG) == null) {
            (new AboutFragment()).show(manager, FRAGMENT_ABOUT_TAG);
        }
    }

    private void showNoConnectionMessage() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentByTag(FRAGMENT_NO_CONNECTION_TAG) == null) {
            (new NoConnectionFragment()).show(manager, FRAGMENT_NO_CONNECTION_TAG);
        }
    }

    private void hideNoConnectionMessage() {
        FragmentManager manager = getSupportFragmentManager();
        NoConnectionFragment fragment = (NoConnectionFragment) manager.findFragmentByTag(FRAGMENT_NO_CONNECTION_TAG);
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    @Override
    public void requestReconnect() {
        loadVehicles();
        loadForecasts();
    }

    @Override
    public void requestRouteSelection() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentByTag(FRAGMENT_SELECT_ROUTE_TAG) == null) {
            (new SelectRouteFragment()).show(manager, FRAGMENT_SELECT_ROUTE_TAG);
        }
    }

    @Override
    public boolean isRouteSelected(Transport transport, Route route) {
        for (SelectedRouteInfo info: selectedRoutes) {
            if (info.transport.equals(transport) && info.route.equals(route)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void selectRoutes(List<SelectedRouteInfo> selected) {
        vehiclePoller.stop();
        selectedRoutes = selected;
        (new SelectedRouteStore(this)).put(selected);
        if (vehicles != null) {
            if (cleanupVehicles()) {
                listeners.notify(EVENT_LISTENER_KEY_ON_LOAD_VEHICLES, new EventListenerManager.OnIterateListener() {
                    @Override
                    public void onIterate(EventListener listener) {
                        ((OnLoadVehiclesListener) listener).onLoadVehicles(vehicles);
                    }
                });
            }
        }
        if (selectedRoutes.isEmpty()) {
            listeners.notify(EVENT_LISTENER_KEY_ON_UNSELECT_ALL_ROUTES, new EventListenerManager.OnIterateListener() {
                @Override
                public void onIterate(EventListener listener) {
                    ((OnUnselectAllRoutesListener) listener).onUnselectAllRoutes();
                }
            });
        }
        loadVehicles();
    }

    private boolean cleanupVehicles() {
        int count = vehicles.size();
        Iterator<RouteInfo> iterator = vehicles.iterator();
        while (iterator.hasNext()) {
            RouteInfo info = iterator.next();
            if (!isRouteSelected(info.transport, info.route)) {
                iterator.remove();
            }
        }
        return vehicles.size() != count;
    }

    private void loadVehicles() {
        vehiclePoller.stop();
        if (service != null && !selectedRoutes.isEmpty()) {
            vehiclePoller.start(service, selectedRoutes);
        }
    }

    @Override
    public void addOnUnselectAllRoutesListener(OnUnselectAllRoutesListener listener) {
        listeners.add(EVENT_LISTENER_KEY_ON_UNSELECT_ALL_ROUTES, listener);
        if (selectedRoutes.isEmpty()) {
            listener.onUnselectAllRoutes();
        }
    }

    @Override
    public void removeOnUnselectAllRoutesListener(OnUnselectAllRoutesListener listener) {
        listeners.remove(EVENT_LISTENER_KEY_ON_UNSELECT_ALL_ROUTES, listener);
    }

    @Override
    public void addOnLoadServiceListener(OnLoadServiceListener listener) {
        listeners.add(EVENT_LISTENER_KEY_ON_LOAD_SERVICE, listener);
        listener.onLoadService(service);
    }

    @Override
    public void removeOnLoadServiceListener(OnLoadServiceListener listener) {
        listeners.remove(EVENT_LISTENER_KEY_ON_LOAD_SERVICE, listener);
    }

    @Override
    public void addOnLoadVehiclesListener(OnLoadVehiclesListener listener) {
        listeners.add(EVENT_LISTENER_KEY_ON_LOAD_VEHICLES, listener);
        if (vehicles != null) {
            listener.onLoadVehicles(vehicles);
        }
    }

    @Override
    public void removeOnLoadVehiclesListener(OnLoadVehiclesListener listener) {
        listeners.remove(EVENT_LISTENER_KEY_ON_LOAD_VEHICLES, listener);
    }

    @Override
    public void requestStationSelection() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentByTag(FRAGMENT_SELECT_STATION_TAG) == null) {
            (new SelectStationFragment()).show(manager, FRAGMENT_SELECT_STATION_TAG);
        }
    }

    @Override
    public void selectStation(final SelectedStationInfo selected) {
        if (isStationSelected(selected.transport, selected.station)) {
            return;
        }
        forecastPoller.stop();
        selectedStations.add(selected);
        (new SelectedStationStore(this)).put(selectedStations);
        listeners.notify(EVENT_LISTENER_KEY_ON_SELECT_STATION, new EventListenerManager.OnIterateListener() {
            @Override
            public void onIterate(EventListener listener) {
                ((OnSelectStationListener) listener).onSelectStation(selected);
            }
        });
        loadForecasts();
    }

    private boolean isStationSelected(Transport transport, Station station) {
        for (SelectedStationInfo info: selectedStations) {
            if (info.transport.equals(transport) && info.station.equals(station)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void unselectStation(final Transport transport, final Station station) {
        forecastPoller.stop();
        removeSelectedStation(transport, station);
        (new SelectedStationStore(this)).put(selectedStations);
        listeners.notify(EVENT_LISTENER_KEY_ON_UNSELECT_STATION, new EventListenerManager.OnIterateListener() {
            @Override
            public void onIterate(EventListener listener) {
                ((OnUnselectStationListener) listener).onUnselectStation(transport, station);
            }
        });
        if (forecasts != null) {
            if (cleanupForecasts(transport, station)) {
                listeners.notify(EVENT_LISTENER_KEY_ON_LOAD_FORECASTS, new EventListenerManager.OnIterateListener() {
                    @Override
                    public void onIterate(EventListener listener) {
                        ((OnLoadForecastsListener) listener).onLoadForecasts(forecasts);
                    }
                });
            }
        }
        if (selectedStations.isEmpty()) {
            listeners.notify(EVENT_LISTENER_KEY_ON_UNSELECT_ALL_STATIONS, new EventListenerManager.OnIterateListener() {
                @Override
                public void onIterate(EventListener listener) {
                    ((OnUnselectAllStationsListener) listener).onUnselectAllStations();
                }
            });
        }
        loadForecasts();
    }

    private void removeSelectedStation(Transport transport, Station station) {
        Iterator<SelectedStationInfo> iterator = selectedStations.iterator();
        while (iterator.hasNext()) {
            SelectedStationInfo info = iterator.next();
            if (info.transport.equals(transport) && info.station.equals(station)) {
                iterator.remove();
            }
        }
    }

    private boolean cleanupForecasts(Transport transport, Station station) {
        int count = forecasts.size();
        Iterator<Forecast> iterator = forecasts.iterator();
        while (iterator.hasNext()) {
            Forecast forecast = iterator.next();
            if (forecast.transport.equals(transport) && forecast.station.equals(station)) {
                iterator.remove();
            }
        }
        return forecasts.size() != count;
    }

    private void loadForecasts() {
        forecastPoller.stop();
        if (service != null && !selectedStations.isEmpty()) {
            forecastPoller.start(service, selectedStations);
        }
    }

    @Override
    public void addOnLoadStationsListener(OnLoadStationsListener listener) {
        listeners.add(EVENT_LISTENER_KEY_ON_LOAD_STATIONS, listener);
        listener.onLoadStations(selectedStations);
    }

    @Override
    public void removeOnLoadStationsListener(OnLoadStationsListener listener) {
        listeners.remove(EVENT_LISTENER_KEY_ON_LOAD_STATIONS, listener);
    }

    @Override
    public void addOnSelectStationListener(OnSelectStationListener listener) {
        listeners.add(EVENT_LISTENER_KEY_ON_SELECT_STATION, listener);
    }

    @Override
    public void removeOnSelectStationListener(OnSelectStationListener listener) {
        listeners.remove(EVENT_LISTENER_KEY_ON_SELECT_STATION, listener);
    }

    @Override
    public void addOnUnselectStationListener(OnUnselectStationListener listener) {
        listeners.add(EVENT_LISTENER_KEY_ON_UNSELECT_STATION, listener);
    }

    @Override
    public void removeOnUnselectStationListener(OnUnselectStationListener listener) {
        listeners.remove(EVENT_LISTENER_KEY_ON_UNSELECT_STATION, listener);
    }

    @Override
    public void addOnUnselectAllStationsListener(OnUnselectAllStationsListener listener) {
        listeners.add(EVENT_LISTENER_KEY_ON_UNSELECT_ALL_STATIONS, listener);
        if (selectedStations.isEmpty()) {
            listener.onUnselectAllStations();
        }
    }

    @Override
    public void removeOnUnselectAllStationsListener(OnUnselectAllStationsListener listener) {
        listeners.remove(EVENT_LISTENER_KEY_ON_UNSELECT_ALL_STATIONS, listener);
    }

    @Override
    public void addOnLoadForecastsListener(OnLoadForecastsListener listener) {
        listeners.add(EVENT_LISTENER_KEY_ON_LOAD_FORECASTS, listener);
        if (forecasts != null) {
            listener.onLoadForecasts(forecasts);
        }
    }

    @Override
    public void removeOnLoadForecastsListener(OnLoadForecastsListener listener) {
        listeners.remove(EVENT_LISTENER_KEY_ON_LOAD_FORECASTS, listener);
    }

    @Override
    public void makeDonation(DonateItem item) {
        donateManager.makeDonation(item);
    }

    @Override
    public void addOnLoadDonateItemsListener(OnLoadDonateItemsListener listener) {
        listeners.add(EVENT_LISTENER_KEY_ON_LOAD_DONATE_ITEMS, listener);
        listener.onLoadDonateItems(donateManager.getItems());
    }

    @Override
    public void removeOnLoadDonateItemsListener(OnLoadDonateItemsListener listener) {
        listeners.remove(EVENT_LISTENER_KEY_ON_LOAD_DONATE_ITEMS, listener);
    }
}
