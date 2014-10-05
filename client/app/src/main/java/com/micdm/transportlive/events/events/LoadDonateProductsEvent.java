package com.micdm.transportlive.events.events;

import com.micdm.transportlive.donate.DonateProduct;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

import java.util.List;

public class LoadDonateProductsEvent extends Event {

    private final List<DonateProduct> products;

    public LoadDonateProductsEvent(List<DonateProduct> products) {
        super(EventType.LOAD_DONATE_PRODUCTS);
        this.products = products;
    }

    public List<DonateProduct> getProducts() {
        return products;
    }
}
