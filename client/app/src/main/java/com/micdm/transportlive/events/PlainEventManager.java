package com.micdm.transportlive.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PlainEventManager implements EventManager {

    private static class Subscriber {

        private final Object key;
        private final OnEventListener listener;

        public Subscriber(Object key, OnEventListener listener) {
            this.key = key;
            this.listener = listener;
        }

        public Object getKey() {
            return key;
        }

        public OnEventListener getListener() {
            return listener;
        }
    }

    private final Map<EventType, List<Subscriber>> subscribers = new HashMap<>();

    @Override
    public void subscribe(Object key, EventType type, OnEventListener<? extends Event> listener) {
        List<Subscriber> subscribers = this.subscribers.get(type);
        if (subscribers == null) {
            subscribers = new ArrayList<>();
            this.subscribers.put(type, subscribers);
        }
        subscribers.add(new Subscriber(key, listener));
    }

    @Override
    public void unsubscribeAll(Object key) {
        for (Map.Entry<EventType, List<Subscriber>> item: this.subscribers.entrySet()) {
            Iterator<Subscriber> iterator = item.getValue().iterator();
            while (iterator.hasNext()) {
                Subscriber subscriber = iterator.next();
                if (subscriber.getKey() == key) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void publish(Event event) {
        List<Subscriber> subscribers = this.subscribers.get(event.getType());
        if (subscribers != null) {
            for (Subscriber subscriber: subscribers) {
                subscriber.getListener().onEvent(event);
            }
        }
    }
}
