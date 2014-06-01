package com.micdm.transportlive.misc;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.micdm.transportlive.R;

public class Analytics {

    public static enum Category {
        TABS,
        DIALOGS,
        DONATE,
        MISC
    }

    public static enum Action {
        SHOW,
        CLICK
    }

    private final Context context;
    private final Tracker tracker;

    public Analytics(Context context) {
        this.context = context;
        this.tracker = GoogleAnalytics.getInstance(context).newTracker(R.xml.tracker);
    }

    public void reportActivityStart(Activity activity) {
        GoogleAnalytics.getInstance(context).reportActivityStart(activity);
    }

    public void reportActivityStop(Activity activity) {
        GoogleAnalytics.getInstance(context).reportActivityStop(activity);
    }

    public void reportEvent(Category category, Action action, String label) {
        HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
        builder.setCategory(category.toString().toLowerCase());
        builder.setAction(action.toString().toLowerCase());
        builder.setLabel(label);
        tracker.send(builder.build());
    }
}
