package com.micdm.transportlive.misc.analytics;

import android.app.Activity;
import android.content.Context;

public abstract class Analytics {

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

    protected final Context context;

    public Analytics(Context context) {
        this.context = context;
    }

    public abstract void reportActivityStart(Activity activity);
    public abstract void reportActivityStop(Activity activity);
    public abstract void reportEvent(Category category, Action action, String label);
}
