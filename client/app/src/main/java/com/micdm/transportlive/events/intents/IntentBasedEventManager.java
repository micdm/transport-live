package com.micdm.transportlive.events.intents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventManager;
import com.micdm.transportlive.events.EventType;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class IntentBasedEventManager implements EventManager {

    private final EventConverter eventConverter;
    private final IntentConverter intentConverter;

    private final LocalBroadcastManager manager;
    private final Map<Object, List<BroadcastReceiver>> receivers = new Hashtable<Object, List<BroadcastReceiver>>();

    public IntentBasedEventManager(Context context) {
        eventConverter = new EventConverter(context);
        intentConverter = new IntentConverter();
        manager = LocalBroadcastManager.getInstance(context);
    }

    @Override
    public void subscribe(Object key, EventType type, final OnEventListener listener) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                listener.onEvent(intentConverter.convert(intent));
            }
        };
        manager.registerReceiver(receiver, getIntentFilter(type));
        if (!receivers.containsKey(key)) {
            receivers.put(key, new ArrayList<BroadcastReceiver>());
        }
        receivers.get(key).add(receiver);
    }

    private IntentFilter getIntentFilter(EventType type) {
        return new IntentFilter(eventConverter.getIntentAction(type));
    }

    @Override
    public void unsubscribeAll(Object key) {
        for (BroadcastReceiver receiver: receivers.get(key)) {
            manager.unregisterReceiver(receiver);
        }
        receivers.remove(key);
    }

    @Override
    public void publish(Event event) {
        Intent intent = eventConverter.convert(event);
        manager.sendBroadcast(intent);
    }
}
