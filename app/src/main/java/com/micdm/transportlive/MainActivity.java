package com.micdm.transportlive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.data.VehicleInfo;
import com.micdm.transportlive.fragments.AboutFragment;
import com.micdm.transportlive.fragments.ForecastFragment;
import com.micdm.transportlive.fragments.SelectStationFragment;
import com.micdm.transportlive.misc.ForecastHandler;
import com.micdm.transportlive.misc.SelectedRouteStore;
import com.micdm.transportlive.misc.SelectedStationStore;
import com.micdm.transportlive.misc.ServiceHandler;
import com.micdm.transportlive.misc.ServiceLoader;
import com.micdm.transportlive.misc.VehiclePoller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends ActionBarActivity implements ServiceHandler, ForecastHandler {

    private static class CustomPagerAdapter extends FragmentPagerAdapter {

        private List<Page> pages = new ArrayList<Page>();

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

    private static class Page {

        public String title;
        public Fragment fragment;

        public Page(String title, Fragment fragment) {
            this.title = title;
            this.fragment = fragment;
        }
    }

    private VehiclePoller poller = new VehiclePoller(this, new VehiclePoller.OnLoadListener() {
        @Override
        public void onStart() {
            if (onLoadVehiclesListener != null) {
                onLoadVehiclesListener.onStart();
            }
        }
        @Override
        public void onFinish() {
            if (onLoadVehiclesListener != null) {
                onLoadVehiclesListener.onFinish();
            }
        }
        @Override
        public void onLoad(List<VehicleInfo> loaded) {
            vehicles = loaded;
            if (onLoadVehiclesListener != null) {
                onLoadVehiclesListener.onLoadVehicles(vehicles);
            }
        }
        @Override
        public void onError() {
            poller.stop();
            onLoadVehiclesListener.onError();
        }
    });
    private Service service;
    private List<VehicleInfo> vehicles;
    private List<SelectedRouteInfo> selectedRoutes;
    private OnUnselectAllRoutesListener onUnselectAllRoutesListener;
    private OnLoadServiceListener onLoadServiceListener;
    private OnLoadVehiclesListener onLoadVehiclesListener;

    private SelectedStationInfo selectedStation;
    private OnSelectStationListener onSelectStationListener;
    private OnLoadForecastListener onLoadForecastListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();
        setupPager();
        loadData();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }

    private void setupPager() {
        CustomViewPager pager = (CustomViewPager) findViewById(R.id.pager);
        if (pager != null) {
            pager.setAdapter(new CustomPagerAdapter(getSupportFragmentManager()));
            pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int i) {
                    getSupportActionBar().setSelectedNavigationItem(i);
                }
            });
            //addPage(new Page(getString(R.string.tab_title_route_list), new RouteListFragment()));
            //addPage(new Page(getString(R.string.tab_title_map), new MapFragment()));
            addPage(new Page(getString(R.string.tab_title_forecast), new ForecastFragment()));
        }
    }

    private void addPage(Page page) {
        final CustomViewPager pager = (CustomViewPager) findViewById(R.id.pager);
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
        if (onUnselectAllRoutesListener != null && selectedRoutes.isEmpty()) {
            onUnselectAllRoutesListener.onUnselectAllRoutes();
        }
        if (onLoadServiceListener != null) {
            onLoadServiceListener.onLoadService(service);
        }
        selectedStation = (new SelectedStationStore(this)).load(service);
        if (onSelectStationListener != null) {
            onSelectStationListener.onSelectStation(selectedStation);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!selectedRoutes.isEmpty()) {
            poller.start(selectedRoutes);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        poller.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.common, menu);
        setupShareMenuItem(menu);
        return true;
    }

    private void setupShareMenuItem(Menu menu) {
        MenuItem item = menu.findItem(R.id.share);
        ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text, getPackageName()));
        provider.setShareIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                (new AboutFragment()).show(getSupportFragmentManager(), "about");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void loadVehicles() {
        poller.start(selectedRoutes);
    }

    @Override
    public Service getService() {
        return service;
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
    public void selectRoute(Transport transport, Route route, boolean isSelected) {
        poller.stop();
        if (isSelected) {
            addSelectedRoute(transport, route);
        } else {
            removeSelectedRoute(transport, route);
            if (vehicles != null) {
                removeVehicles(transport, route);
            }
            if (onLoadVehiclesListener != null && vehicles != null) {
                onLoadVehiclesListener.onLoadVehicles(vehicles);
            }
        }
        (new SelectedRouteStore(this)).put(selectedRoutes);
        if (onUnselectAllRoutesListener != null && selectedRoutes.isEmpty()) {
            onUnselectAllRoutesListener.onUnselectAllRoutes();
        }
        if (!selectedRoutes.isEmpty()) {
            poller.start(selectedRoutes);
        }
    }

    private void addSelectedRoute(Transport transport, Route route) {
        if (!isRouteSelected(transport, route)) {
            selectedRoutes.add(new SelectedRouteInfo(transport, route));
        }
    }

    private void removeSelectedRoute(Transport transport, Route route) {
        if (isRouteSelected(transport, route)) {
            for (SelectedRouteInfo info: selectedRoutes) {
                if (info.transport.equals(transport) && info.route.equals(route)) {
                    selectedRoutes.remove(info);
                    break;
                }
            }
        }
    }

    private void removeVehicles(Transport transport, Route route) {
        Iterator<VehicleInfo> iterator = vehicles.iterator();
        while (iterator.hasNext()) {
            VehicleInfo info = iterator.next();
            if (info.transport.equals(transport) && info.route.equals(route)) {
                iterator.remove();
            }
        }
    }

    @Override
    public void setOnUnselectAllRoutesListener(OnUnselectAllRoutesListener listener) {
        onUnselectAllRoutesListener = listener;
        if (listener != null && selectedRoutes.isEmpty()) {
            listener.onUnselectAllRoutes();
        }
    }

    @Override
    public void setOnLoadServiceListener(OnLoadServiceListener listener) {
        onLoadServiceListener = listener;
        if (listener != null) {
            listener.onLoadService(service);
        }
    }

    @Override
    public void setOnLoadVehiclesListener(OnLoadVehiclesListener listener) {
        onLoadVehiclesListener = listener;
        if (listener != null && vehicles != null) {
            listener.onLoadVehicles(vehicles);
        }
    }

    @Override
    public void requestStationSelection() {
        FragmentManager manager = getSupportFragmentManager();
        SelectStationFragment fragment = (SelectStationFragment) manager.findFragmentByTag("select_station");
        if (fragment != null) {
            fragment.dismiss();
        }
        (new SelectStationFragment()).show(manager, "select_station");
    }

    @Override
    public void selectStation(SelectedStationInfo selected) {
        selectedStation = selected;
        (new SelectedStationStore(this)).put(selectedStation);
        if (onSelectStationListener != null) {
            onSelectStationListener.onSelectStation(selectedStation);
        }
        // TODO: запустить загрузку
    }

    @Override
    public void setOnSelectStationListener(OnSelectStationListener listener) {
        onSelectStationListener = listener;
        if (listener != null) {
            listener.onSelectStation(selectedStation);
        }
    }

    @Override
    public void setOnLoadForecastListener(OnLoadForecastListener listener) {
        onLoadForecastListener = listener;
        // TODO: если прогноз загружен, вызвать слушатель
    }
}
