package com.micdm.transportlive.donate;

public class DonateProduct {

    private final String id;
    private final String price;
    private final String title;

    public DonateProduct(String id, String price, String title) {
        this.id = id;
        this.price = price;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getPrice() {
        return price;
    }

    public String getTitle() {
        return title;
    }
}
