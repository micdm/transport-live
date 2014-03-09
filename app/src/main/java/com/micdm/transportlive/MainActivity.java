package com.micdm.transportlive;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.RouteInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.fragments.MapFragment;
import com.micdm.transportlive.fragments.ProgressFragment;
import com.micdm.transportlive.fragments.RouteListFragment;
import com.micdm.transportlive.misc.ServiceCache;
import com.micdm.transportlive.misc.ServiceHandler;
import com.micdm.transportlive.misc.ServiceLoader;
import com.micdm.transportlive.misc.ServicePoller;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements ServiceHandler {

    private static class CustomPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Page> pages = new ArrayList<Page>();

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

    private ServiceCache cache = new ServiceCache(this);
    private ServiceLoader loader = new ServiceLoader(this, new ServiceLoader.OnNoConnectionListener() {
        @Override
        public void onNoConnection() {
            
        }
    });
    private ServicePoller poller = new ServicePoller(loader, new ServicePoller.OnLoadListener() {
        @Override
        public void onLoad(Service service) {
            if (onLoadVehiclesListener != null) {
                onLoadVehiclesListener.onLoadVehicles(service);
            }
        }
    });
    private OnUnselectAllRoutesListener onUnselectAllRoutesListener;
    private OnLoadServiceListener onLoadServiceListener;
    private OnLoadVehiclesListener onLoadVehiclesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();
        setupPager();
        loadService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Service service = cache.get();
        if (service != null && getSelectedRouteCount(service) != 0) {
            poller.start(service);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        poller.stop();
    }

    private int getSelectedRouteCount(Service service) {
        int count = 0;
        for (Transport transport: service.transports) {
            for (Route route: transport.routes) {
                if (route.isSelected) {
                    count += 1;
                }
            }
        }
        return count;
    }

    private void loadService() {
        Service service = cache.get();
        if (service == null) {
            loadTransports(new Service());
        } else {
            onLoadService(service);
            if (getSelectedRouteCount(service) != 0) {
                poller.start(service);
            }
        }
    }

    private void loadTransports(Service service) {
        showLoadingMessage(getString(R.string.first_loading_transports));
        loader.loadTransports(service, new ServiceLoader.OnLoadListener() {
            @Override
            public void onLoad(Service service) {
                hideLoadingMessage();
                loadRoutes(service);
            }
        });
    }

    private void loadRoutes(Service service) {
        showLoadingMessage(getString(R.string.first_loading_routes));
        loader.loadRoutes(service, new ServiceLoader.OnLoadListener() {
            @Override
            public void onLoad(Service service) {
                hideLoadingMessage();
                loadStations(service);
            }
        });
    }

    private void loadStations(Service service) {
        showLoadingMessage(getString(R.string.first_loading_stations));
        loader.loadStations(service, new ServiceLoader.OnLoadListener() {
            @Override
            public void onLoad(Service service) {
                hideLoadingMessage();
                cache.put(service);
                onLoadService(service);
            }
        });
    }

    private void onLoadService(Service service) {
        if (onUnselectAllRoutesListener != null && getSelectedRouteCount(service) == 0) {
            onUnselectAllRoutesListener.onUnselectAllRoutes();
        }
        if (onLoadServiceListener != null) {
            onLoadServiceListener.onLoadService(service);
        }
    }

    private void showLoadingMessage(String message) {
        ProgressFragment fragment = new ProgressFragment();
        Bundle arguments = new Bundle();
        arguments.putString("message", message);
        fragment.setArguments(arguments);
        fragment.show(getSupportFragmentManager(), "progress");
    }

    private void hideLoadingMessage() {
        ProgressFragment fragment = (ProgressFragment) getSupportFragmentManager().findFragmentByTag("progress");
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    private void setupPager() {
        final ActionBar actionBar = getSupportActionBar();
        final CustomViewPager pager = (CustomViewPager) findViewById(R.id.pager);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int i) {
                actionBar.setSelectedNavigationItem(i);
            }
        });
        CustomPagerAdapter adapter = new CustomPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        addPage(new Page(getString(R.string.tab_title_route_list), new RouteListFragment()));
        addPage(new Page(getString(R.string.tab_title_map), new MapFragment()));
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

    @Override
    public Service getService() {
        return cache.get();
    }

    @Override
    public void selectRoute(RouteInfo info, boolean isSelected) {
        poller.stop();
        Service service = cache.get();
        Transport transport = service.getTransportByType(info.transport);
        Route route = transport.getRouteByNumber(info.route.number);
        route.isSelected = isSelected;
        if (!isSelected) {
            for (Direction direction: route.directions) {
                direction.vehicles.clear();
            }
        }
        cache.put(service);
        if (getSelectedRouteCount(service) == 0) {
            if (onUnselectAllRoutesListener != null) {
                onUnselectAllRoutesListener.onUnselectAllRoutes();
            }
        } else {
            poller.start(service);
        }
    }

    @Override
    public void setOnUnselectAllRoutesListener(OnUnselectAllRoutesListener listener) {
        onUnselectAllRoutesListener = listener;
        Service service = cache.get();
        if (service != null && getSelectedRouteCount(service) == 0) {
            listener.onUnselectAllRoutes();
        }
    }

    @Override
    public void setOnLoadServiceListener(OnLoadServiceListener listener) {
        onLoadServiceListener = listener;
        Service service = cache.get();
        if (service != null) {
            listener.onLoadService(service);
        }
    }

    @Override
    public void setOnLoadVehiclesListener(OnLoadVehiclesListener listener) {
        onLoadVehiclesListener = listener;
        Service service = cache.get();
        if (service != null && getSelectedRouteCount(service) != 0) {
            listener.onLoadVehicles(service);
        }
    }
}
