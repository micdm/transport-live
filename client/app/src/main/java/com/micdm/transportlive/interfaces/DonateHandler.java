package com.micdm.transportlive.interfaces;

import com.micdm.transportlive.donate.DonateItem;

import java.util.List;

public interface DonateHandler {

    public static interface OnLoadDonateItemsListener extends EventListener {
        public void onLoadDonateItems(List<DonateItem> items);
    }

    public static interface OnDonateListener extends EventListener {
        public void onDonate();
    }

    public void makeDonation(DonateItem item);
    public void addOnLoadDonateItemsListener(OnLoadDonateItemsListener listener);
    public void removeOnLoadDonateItemsListener(OnLoadDonateItemsListener listener);
    public void addOnDonateListener(OnDonateListener listener);
    public void removeOnDonateListener(OnDonateListener listener);
}
