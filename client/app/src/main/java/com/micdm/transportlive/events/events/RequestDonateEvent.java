package com.micdm.transportlive.events.events;

import com.micdm.transportlive.donate.DonateProduct;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class RequestDonateEvent extends Event {

    private final DonateProduct product;

    public RequestDonateEvent(DonateProduct product) {
        super(EventType.REQUEST_DONATE);
        this.product = product;
    }

    public DonateProduct getProduct() {
        return product;
    }
}
