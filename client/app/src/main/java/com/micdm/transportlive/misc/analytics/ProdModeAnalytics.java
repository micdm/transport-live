package com.micdm.transportlive.misc.analytics;

import android.app.Activity;
import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

public class ProdModeAnalytics implements Analytics {

    private final EasyTracker tracker;

    public ProdModeAnalytics(Context context) {
        tracker = EasyTracker.getInstance(context);
    }

    @Override
    public void reportActivityStart(Activity activity) {
        tracker.activityStart(activity);
    }

    @Override
    public void reportActivityStop(Activity activity) {
        tracker.activityStop(activity);
    }

    @Override
    public void reportEvent(Category category, Action action, String label) {
        reportEvent(category, action, label, null);
    }

    @Override
    public void reportEvent(Category category, Action action, String label, Integer value) {
        MapBuilder builder = MapBuilder.createEvent(category.toString().toLowerCase(), action.toString().toLowerCase(), label, null);
        if (value != null) {
            builder.set(Fields.EVENT_VALUE, value.toString());
        }
        tracker.send(builder.build());
    }
}
