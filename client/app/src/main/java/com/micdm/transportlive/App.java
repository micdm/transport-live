package com.micdm.transportlive;

import android.content.Context;

import com.micdm.transportlive.events.EventManager;
import com.micdm.transportlive.events.intents.IntentBasedEventManager;
import com.micdm.transportlive.misc.analytics.Analytics;
import com.micdm.transportlive.misc.analytics.DevModeAnalytics;
import com.micdm.transportlive.misc.analytics.ProdModeAnalytics;
import com.micdm.transportlive.server2.ServerGate;

public class App extends android.app.Application {

    private static App instance;

    private EventManager eventManager;
    private ServerGate serverGate;
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

    public boolean isDevMode() {
        return getPackageName().endsWith(".dev");
    }

    public EventManager getEventManager() {
        if (eventManager == null) {
            eventManager = new IntentBasedEventManager(getApplicationContext());
        }
        return eventManager;
    }

    public ServerGate getServerGate() {
        if (serverGate == null) {
            serverGate = new ServerGate(getApplicationContext());
        }
        return serverGate;
    }

    public Analytics getAnalytics() {
        if (analytics == null) {
            Context context = getApplicationContext();
            analytics = isDevMode() ? new DevModeAnalytics(context) : new ProdModeAnalytics(context);
        }
        return analytics;
    }
}
