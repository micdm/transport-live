package com.micdm.transportlive.location;

import android.content.Context;
import android.location.Location;

public abstract class Locator {

    public static interface OnUpdateLocationListener {
        public void onUpdateLocation(Location location);
    }

    protected static final int MIN_UPDATE_TIME = 5;
    protected static final int MIN_UPDATE_DISTANCE = 50;

    protected final Context context;
    protected OnUpdateLocationListener onUpdateLocationListener;

    public Locator(Context context) {
        this.context = context;
    }

    public void setOnUpdateLocationListener(OnUpdateLocationListener onUpdateLocationListener) {
        this.onUpdateLocationListener = onUpdateLocationListener;
    }

    public abstract void start();
    public abstract void stop();
}
