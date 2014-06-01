package com.micdm.transportlive;

import android.app.Application;

import com.micdm.transportlive.misc.Analytics;

public class CustomApplication extends Application {

    private Analytics analytics;

    @Override
    public void onCreate() {
        super.onCreate();
        analytics = new Analytics(this);
    }

    public Analytics getAnalytics() {
        return analytics;
    }
}
