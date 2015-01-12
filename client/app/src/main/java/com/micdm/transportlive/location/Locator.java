package com.micdm.transportlive.location;

import android.location.Location;

public abstract class Locator {

    public static interface OnUpdateLocationListener {
        public void onUpdateLocation(Location location);
    }

    protected static final int MIN_UPDATE_TIME = 5;
    protected static final int MIN_UPDATE_DISTANCE = 50;

    protected OnUpdateLocationListener onUpdateLocationListener;

    public void setOnUpdateLocationListener(OnUpdateLocationListener onUpdateLocationListener) {
        this.onUpdateLocationListener = onUpdateLocationListener;
    }

    public abstract void start();
    public abstract void stop();
}
