package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RequestLoadDonateProductsEvent extends Event {

    public RequestLoadDonateProductsEvent() {
        super(EventType.REQUEST_LOAD_DONATE_PRODUCTS);
    }
}
