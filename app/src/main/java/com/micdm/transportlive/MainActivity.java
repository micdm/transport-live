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

import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.RouteInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.fragments.AboutFragment;
import com.micdm.transportlive.fragments.MapFragment;
import com.micdm.transportlive.fragments.RouteListFragment;
import com.micdm.transportlive.misc.ServiceHandler;
import com.micdm.transportlive.misc.ServiceLoader;
import com.micdm.transportlive.misc.VehiclePoller;

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

    private ServiceLoader loader = new ServiceLoader(this);
    private VehiclePoller poller = new VehiclePoller(this, new VehiclePoller.OnLoadListener() {
        @Override
        public void onLoad(Service service) {
            if (onLoadVehiclesListener != null) {
                onLoadVehiclesListener.onLoadVehicles(service);
            }
        }
        @Override
        public void onNoConnection() {

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
        Service service = loader.load();
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
        Service service = loader.load();
        if (onUnselectAllRoutesListener != null && getSelectedRouteCount(service) == 0) {
            onUnselectAllRoutesListener.onUnselectAllRoutes();
        }
        if (onLoadServiceListener != null) {
            onLoadServiceListener.onLoadService(service);
        }
        if (getSelectedRouteCount(service) != 0) {
            poller.start(service);
        }
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
            addPage(new Page(getString(R.string.tab_title_route_list), new RouteListFragment()));
            addPage(new Page(getString(R.string.tab_title_map), new MapFragment()));
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
    public Service getService() {
        return loader.load();
    }

    @Override
    public void selectRoute(RouteInfo info, boolean isSelected) {
        poller.stop();
        Service service = loader.load();
        Transport transport = service.getTransportByType(info.transport);
        Route route = transport.getRouteByNumber(info.route.number);
        route.isSelected = isSelected;
        if (!isSelected) {
            for (Direction direction: route.directions) {
                direction.vehicles.clear();
            }
        }
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
        if (listener != null) {
            Service service = loader.load();
            if (service != null && getSelectedRouteCount(service) == 0) {
                listener.onUnselectAllRoutes();
            }
        }
    }

    @Override
    public void setOnLoadServiceListener(OnLoadServiceListener listener) {
        onLoadServiceListener = listener;
        if (listener != null) {
            Service service = loader.load();
            if (service != null) {
                listener.onLoadService(service);
            }
        }
    }

    @Override
    public void setOnLoadVehiclesListener(OnLoadVehiclesListener listener) {
        onLoadVehiclesListener = listener;
        if (listener != null) {
            Service service = loader.load();
            if (service != null && getSelectedRouteCount(service) != 0) {
                listener.onLoadVehicles(service);
            }
        }
    }
}
