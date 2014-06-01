package com.micdm.transportlive.donate;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.android.vending.billing.IInAppBillingService;

public class BillingServiceConnection implements ServiceConnection {

    public static interface OnServiceReadyListener {
        public void onServiceReady();
    }

    private IInAppBillingService service;
    private final OnServiceReadyListener listener;

    public BillingServiceConnection(OnServiceReadyListener listener) {
        this.listener = listener;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = IInAppBillingService.Stub.asInterface(binder);
        listener.onServiceReady();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    public IInAppBillingService getService() {
        return service;
    }
}
