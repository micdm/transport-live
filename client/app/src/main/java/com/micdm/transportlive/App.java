package com.micdm.transportlive;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.micdm.transportlive.events.EventManager;
import com.micdm.transportlive.events.intents.IntentBasedEventManager;
import com.micdm.transportlive.location.DefaultLocator;
import com.micdm.transportlive.location.GooglePlayLocator;
import com.micdm.transportlive.location.Locator;
import com.micdm.transportlive.misc.ServiceLoader;
import com.micdm.transportlive.misc.analytics.Analytics;
import com.micdm.transportlive.misc.analytics.DevModeAnalytics;
import com.micdm.transportlive.misc.analytics.ProdModeAnalytics;
import com.micdm.transportlive.server.ServerGate;
import com.micdm.transportlive.stores.SelectedRouteStore;
import com.micdm.transportlive.stores.SelectedStationStore;

public class App extends android.app.Application {

    private static App instance;

    private ServerGate serverGate;
    private EventManager eventManager;
    private ServiceLoader serviceLoader;
    private SelectedRouteStore selectedRouteStore;
    private SelectedStationStore selectedStationStore;
    private Locator locator;
    private Analytics analytics;

    public static App get() {
        if (instance == null) {
            throw new RuntimeException("application not ready yet");
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public ServerGate getServerGate() {
        if (serverGate == null) {
            serverGate = new ServerGate(getApplicationContext());
        }
        return serverGate;
    }

    public EventManager getEventManager() {
        if (eventManager == null) {
            eventManager = new IntentBasedEventManager(getApplicationContext());
        }
        return eventManager;
    }

    public ServiceLoader getServiceLoader() {
        if (serviceLoader == null) {
            serviceLoader = new ServiceLoader(getApplicationContext());
        }
        return serviceLoader;
    }

    public SelectedRouteStore getSelectedRouteStore() {
        if (selectedRouteStore == null) {
            selectedRouteStore = new SelectedRouteStore(getApplicationContext());
        }
        return selectedRouteStore;
    }

    public SelectedStationStore getSelectedStationStore() {
        if (selectedStationStore == null) {
            selectedStationStore = new SelectedStationStore(getApplicationContext());
        }
        return selectedStationStore;
    }

    public Locator getLocator() {
        if (locator == null) {
            Context context = getApplicationContext();
            boolean isPlayServicesAvailable = (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS);
            locator = isPlayServicesAvailable ? new GooglePlayLocator(context) : new DefaultLocator(context);
        }
        return locator;
    }

    public Analytics getAnalytics() {
        if (analytics == null) {
            Context context = getApplicationContext();
            boolean isDevMode = getPackageName().endsWith(".dev");
            analytics = isDevMode ? new DevModeAnalytics(context) : new ProdModeAnalytics(context);
        }
        return analytics;
    }
}
