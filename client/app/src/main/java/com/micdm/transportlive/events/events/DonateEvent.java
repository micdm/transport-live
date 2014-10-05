package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class DonateEvent extends Event {

    public DonateEvent() {
        super(EventType.DONATE);
    }
}
