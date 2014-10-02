package com.micdm.transportlive.activities;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.micdm.transportlive.CustomApplication;
import com.micdm.transportlive.R;
import com.micdm.transportlive.donate.DonateManager;
import com.micdm.transportlive.donate.DonateProduct;
import com.micdm.transportlive.fragments.AboutFragment;
import com.micdm.transportlive.fragments.DonateFragment;
import com.micdm.transportlive.fragments.SettingsFragment;
import com.micdm.transportlive.interfaces.DonateHandler;
import com.micdm.transportlive.interfaces.EventListener;
import com.micdm.transportlive.misc.EventListenerManager;
import com.micdm.transportlive.misc.analytics.Analytics;

import java.util.List;

public class SettingsActivity extends FragmentActivity implements PreferenceFragment.OnPreferenceStartFragmentCallback, DonateHandler {

    private static final String FRAGMENT_ABOUT_TAG = "about";
    private static final String FRAGMENT_DONATE_TAG = "donate";

    private static final String EVENT_LISTENER_KEY_ON_LOAD_DONATE_PRODUCTS = "OnLoadDonateProducts";
    private static final String EVENT_LISTENER_KEY_ON_DONATE = "OnDonate";

    private static final int BUY_REQUEST_CODE = 1001;

    private final EventListenerManager listeners = new EventListenerManager();

    private final DonateManager donateManager = new DonateManager(this, new DonateManager.OnLoadProductsListener() {
        @Override
        public void onLoadProducts(final List<DonateProduct> products) {
            listeners.notify(EVENT_LISTENER_KEY_ON_LOAD_DONATE_PRODUCTS, new EventListenerManager.OnIterateListener() {
                @Override
                public void onIterate(EventListener listener) {
                    ((OnLoadDonateProductsListener) listener).onLoadDonateProducts(products);
                }
            });
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        donateManager.init();
        setContentView(R.layout.a__settings);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment fragment, Preference pref) {
        String key = pref.getKey();
        if (key == null) {
            return false;
        }
        if (key.equals(SettingsFragment.PREF_KEY_DONATE)) {
            showDonateMessage();
            return true;
        }
        if (key.equals(SettingsFragment.PREF_KEY_SHARE)) {
            showShareMessage();
            return true;
        }
        if (key.equals(SettingsFragment.PREF_KEY_ABOUT)) {
            showAboutMessage();
            return true;
        }
        return false;
    }

    private void showDonateMessage() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentByTag(FRAGMENT_DONATE_TAG) == null) {
            (new DonateFragment()).show(manager, FRAGMENT_DONATE_TAG);
            CustomApplication.get().getAnalytics().reportEvent(Analytics.Category.DIALOGS, Analytics.Action.SHOW, "donate");
        }
    }

    private void showShareMessage() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text, getPackageName()));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {}
        CustomApplication.get().getAnalytics().reportEvent(Analytics.Category.DIALOGS, Analytics.Action.SHOW, "share");
    }

    private void showAboutMessage() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentByTag(FRAGMENT_ABOUT_TAG) == null) {
            (new AboutFragment()).show(manager, FRAGMENT_ABOUT_TAG);
            CustomApplication.get().getAnalytics().reportEvent(Analytics.Category.DIALOGS, Analytics.Action.SHOW, "about");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BUY_REQUEST_CODE && resultCode == RESULT_OK) {
            listeners.notify(EVENT_LISTENER_KEY_ON_DONATE, new EventListenerManager.OnIterateListener() {
                @Override
                public void onIterate(EventListener listener) {
                    ((OnDonateListener) listener).onDonate();
                }
            });
            donateManager.handleDonate(data.getStringExtra("INAPP_PURCHASE_DATA"));
        }
    }

    @Override
    public void makeDonation(DonateProduct product) {
        PendingIntent intent = donateManager.getDonateIntent(product);
        if (intent == null) {
            return;
        }
        try {
            startIntentSenderForResult(intent.getIntentSender(), BUY_REQUEST_CODE, new Intent(), 0, 0, 0);
            CustomApplication.get().getAnalytics().reportEvent(Analytics.Category.DONATE, Analytics.Action.CLICK, product.id);
        } catch (IntentSender.SendIntentException e) {}
    }

    @Override
    public void addOnLoadDonateProductsListener(OnLoadDonateProductsListener listener) {
        listeners.add(EVENT_LISTENER_KEY_ON_LOAD_DONATE_PRODUCTS, listener);
        listener.onLoadDonateProducts(donateManager.getProducts());
    }

    @Override
    public void removeOnLoadDonateProductsListener(OnLoadDonateProductsListener listener) {
        listeners.remove(EVENT_LISTENER_KEY_ON_LOAD_DONATE_PRODUCTS, listener);
    }

    @Override
    public void addOnDonateListener(OnDonateListener listener) {
        listeners.add(EVENT_LISTENER_KEY_ON_DONATE, listener);
    }

    @Override
    public void removeOnDonateListener(OnDonateListener listener) {
        listeners.remove(EVENT_LISTENER_KEY_ON_DONATE, listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        donateManager.deinit();
    }
}
