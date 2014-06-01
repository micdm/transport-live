package com.micdm.transportlive.donate;

import android.content.Context;
import android.content.Intent;

import java.util.List;

public class DonateManager {

    public static interface OnLoadItemsListener {
        public void onLoadItems(List<DonateItem> items);
    }

    private static final String[] IDS = new String[] {"1", "2", "3", "4"};

    private final BillingServiceConnection connection = new BillingServiceConnection(new BillingServiceConnection.OnServiceReadyListener() {
        @Override
        public void onServiceReady() {
            loadItems();
        }
    });
    private List<DonateItem> items;

    private final Context context;
    private final OnLoadItemsListener listener;

    public DonateManager(Context context, OnLoadItemsListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void init() {
        context.bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), connection, Context.BIND_AUTO_CREATE);
    }

    public void deinit() {
        context.unbindService(connection);
    }

    public List<DonateItem> getItems() {
        return items;
    }

    private void loadItems() {
        LoadItemsTask task = new LoadItemsTask(context, connection.getService(), new LoadItemsTask.OnLoadItemsListener() {
            @Override
            public void onLoadItems(final List<DonateItem> loaded) {
                items = loaded;
                listener.onLoadItems(loaded);
            }
        });
        task.execute(IDS);
    }

    public void makeDonation(DonateItem item) {

    }
}
