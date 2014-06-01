package com.micdm.transportlive.donate;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;

import java.util.List;

public class DonateManager {

    public static interface OnLoadItemsListener {
        public void onLoadItems(List<DonateItem> items);
    }

    public static final int BILLING_API_VERSION = 3;
    public static final String PURCHASE_TYPE = "inapp";
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

    public PendingIntent getBuyIntent(DonateItem item) {
        try {
            Bundle result = connection.getService().getBuyIntent(BILLING_API_VERSION, context.getPackageName(), item.id, PURCHASE_TYPE, "");
            if (result.getInt("RESPONSE_CODE") != 0) {
                return null;
            }
            return result.getParcelable("BUY_INTENT");
        } catch (RemoteException e) {
            return null;
        }
    }
}
