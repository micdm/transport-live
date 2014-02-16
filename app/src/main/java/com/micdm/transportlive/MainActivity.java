package com.micdm.transportlive;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.fragments.MapFragment;
import com.micdm.transportlive.fragments.NoConnectionFragment;
import com.micdm.transportlive.fragments.RouteListFragment;
import com.micdm.transportlive.fragments.TransportRouteListFragment;
import com.micdm.transportlive.misc.ServiceCache;
import com.micdm.transportlive.misc.ServiceLoader;

public class MainActivity extends ActionBarActivity implements NoConnectionFragment.OnRetryConnectionListener, TransportRouteListFragment.OnRouteCheckChangeListener {

    private static class CustomPagerAdapter extends FragmentPagerAdapter {

        private static final int PAGE_COUNT = 2;

        private static final int PAGE_ROUTE_LIST = 0;
        private static final int PAGE_MAP = 1;

        private Context context;

        public CustomPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int i) {
            if (i == PAGE_ROUTE_LIST) {
                return new RouteListFragment();
            }
            if (i == PAGE_MAP) {
                return new MapFragment();
            }
            throw new RuntimeException("unknown page");
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int i) {
            switch (i) {
                case PAGE_ROUTE_LIST:
                    return context.getString(R.string.tab_title_route_list);
                case PAGE_MAP:
                    return context.getString(R.string.tab_title_map);
                default:
                    throw new RuntimeException("unknown page");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();
        loadService();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    private void loadService() {
        Service service = ServiceCache.get(this);
        if (service == null) {
            ServiceLoader.load(this, new ServiceLoader.OnLoadListener() {
                @Override
                public void onLoad(Service service) {
                    onServiceLoad(service);
                }
                @Override
                public void onNoConnection() {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.add(0, new NoConnectionFragment());
                    transaction.commit();
                }
            });
        } else {
            onServiceLoad(service);
        }
    }

    private void onServiceLoad(Service service) {
        ServiceCache.set(this, service);
        setupPager();
    }

    private void setupPager() {
        final ActionBar actionBar = getSupportActionBar();
        final CustomViewPager pager = (CustomViewPager)findViewById(R.id.pager);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int i) {
                actionBar.setSelectedNavigationItem(i);
            }
        });
        CustomPagerAdapter adapter = new CustomPagerAdapter(this, getSupportFragmentManager());
        for (int i = 0; i < adapter.getCount(); i += 1) {
            ActionBar.Tab tab = actionBar.newTab();
            tab.setText(adapter.getPageTitle(i));
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
        pager.setAdapter(adapter);
    }

    public Service getService() {
        return ServiceCache.get(this);
    }

    @Override
    public void onRetryConnection() {
        loadService();
    }

    @Override
    public void onRouteCheckChange(int transportId, int routeNumber, boolean isChecked) {
        Service service = ServiceCache.get(this);
        Transport transport = service.getTransportById(transportId);
        Route route = transport.getRouteByNumber(routeNumber);
        route.isChecked = isChecked;
        ServiceCache.set(this, service);
    }
}
