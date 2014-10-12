package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RemoveAllDataEvent extends Event {

    public RemoveAllDataEvent() {
        super(EventType.REMOVE_ALL_DATA);
    }
}
