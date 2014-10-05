package com.micdm.transportlive.events;

public interface EventManager {

    public static interface OnEventListener<CustomEvent extends Event> {
        public void onEvent(CustomEvent event);
    }

    public void subscribe(Object key, EventType type, OnEventListener<? extends Event> listener);
    public void unsubscribeAll(Object key);
    public void publish(Event event);
}
