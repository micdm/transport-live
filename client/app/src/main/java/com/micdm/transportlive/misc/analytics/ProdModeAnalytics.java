package com.micdm.transportlive.misc.analytics;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.micdm.transportlive.R;

public class ProdModeAnalytics extends Analytics {

    private final Tracker tracker;

    public ProdModeAnalytics(Context context) {
        super(context);
        tracker = GoogleAnalytics.getInstance(context).newTracker(R.xml.tracker);
    }

    @Override
    public void reportActivityStart(Activity activity) {
        GoogleAnalytics.getInstance(context).reportActivityStart(activity);
    }

    @Override
    public void reportActivityStop(Activity activity) {
        GoogleAnalytics.getInstance(context).reportActivityStop(activity);
    }

    @Override
    public void reportEvent(Category category, Action action, String label) {
        reportEvent(category, action, label, null);
    }

    @Override
    public void reportEvent(Category category, Action action, String label, Integer value) {
        HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
        builder.setCategory(category.toString().toLowerCase());
        builder.setAction(action.toString().toLowerCase());
        builder.setLabel(label);
        if (value != null) {
            builder.setValue(value);
        }
        tracker.send(builder.build());
    }
}
