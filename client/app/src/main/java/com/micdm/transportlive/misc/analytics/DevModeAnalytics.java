package com.micdm.transportlive.misc.analytics;

import android.app.Activity;
import android.content.Context;

public class DevModeAnalytics extends Analytics {

    public DevModeAnalytics(Context context) {
        super(context);
    }

    @Override
    public void reportActivityStart(Activity activity) {}

    @Override
    public void reportActivityStop(Activity activity) {}

    @Override
    public void reportEvent(Category category, Action action, String label) {}
}
