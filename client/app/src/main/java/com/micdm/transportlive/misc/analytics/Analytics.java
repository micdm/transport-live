package com.micdm.transportlive.misc.analytics;

import android.app.Activity;

public interface Analytics {

    public static enum Category {
        TABS,
        DIALOGS,
        DONATE,
        MISC
    }

    public static enum Action {
        SHOW,
        CLICK,
        MISC
    }

    public void reportActivityStart(Activity activity);
    public void reportActivityStop(Activity activity);
    public void reportEvent(Category category, Action action, String label);
    public void reportEvent(Category category, Action action, String label, Integer value);
}
