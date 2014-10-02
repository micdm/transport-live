package com.micdm.transportlive;

import android.app.Application;
import android.content.Context;

import com.micdm.transportlive.misc.analytics.Analytics;
import com.micdm.transportlive.misc.analytics.DevModeAnalytics;
import com.micdm.transportlive.misc.analytics.ProdModeAnalytics;

public class CustomApplication extends Application {

    private static CustomApplication instance;
    private Analytics analytics;

    public static CustomApplication get() {
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

    public Analytics getAnalytics() {
        if (analytics == null) {
            Context context = getApplicationContext();
            analytics = isDevMode() ? new DevModeAnalytics(context) : new ProdModeAnalytics(context);
        }
        return analytics;
    }
}
