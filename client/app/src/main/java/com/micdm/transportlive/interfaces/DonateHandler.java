package com.micdm.transportlive.interfaces;

import com.micdm.transportlive.donate.DonateProduct;

import java.util.List;

public interface DonateHandler {

    public static interface OnLoadDonateProductsListener extends EventListener {
        public void onLoadDonateProducts(List<DonateProduct> products);
    }

    public static interface OnDonateListener extends EventListener {
        public void onDonate();
    }

    public void makeDonation(DonateProduct product);
    public void addOnLoadDonateProductsListener(OnLoadDonateProductsListener listener);
    public void removeOnLoadDonateProductsListener(OnLoadDonateProductsListener listener);
    public void addOnDonateListener(OnDonateListener listener);
    public void removeOnDonateListener(OnDonateListener listener);
}
