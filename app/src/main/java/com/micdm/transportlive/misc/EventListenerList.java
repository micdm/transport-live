package com.micdm.transportlive.misc;

import com.micdm.transportlive.interfaces.EventListener;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class EventListenerList {

    public static interface OnIterateListener {
        public void onIterate(EventListener listener);
    }

    private Hashtable<String, List<EventListener>> listeners = new Hashtable<String, List<EventListener>>();

    public void add(String key, EventListener listener) {
        if (!listeners.containsKey(key)) {
            listeners.put(key, new ArrayList<EventListener>());
        }
        listeners.get(key).add(listener);
    }

    public void remove(String key, EventListener listener) {
        if (listeners.containsKey(key)) {
            listeners.get(key).remove(listener);
        }
    }

    public void notify(String key, OnIterateListener onIterateListener) {
        if (listeners.containsKey(key)) {
            for (EventListener listener: listeners.get(key)) {
                onIterateListener.onIterate(listener);
            }
        }
    }
}
