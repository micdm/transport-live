package com.micdm.transportlive.activities;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.micdm.transportlive.App;
import com.micdm.transportlive.R;
import com.micdm.transportlive.donate.DonateManager;
import com.micdm.transportlive.donate.DonateProduct;
import com.micdm.transportlive.events.EventManager;
import com.micdm.transportlive.events.EventType;
import com.micdm.transportlive.events.events.DonateEvent;
import com.micdm.transportlive.events.events.LoadDonateProductsEvent;
import com.micdm.transportlive.events.events.RequestDonateEvent;
import com.micdm.transportlive.events.events.RequestLoadDonateProductsEvent;
import com.micdm.transportlive.fragments.AboutFragment;
import com.micdm.transportlive.fragments.DonateFragment;
import com.micdm.transportlive.fragments.FragmentTag;
import com.micdm.transportlive.fragments.SettingsFragment;
import com.micdm.transportlive.misc.analytics.Analytics;

import java.util.List;

public class SettingsActivity extends FragmentActivity implements PreferenceFragment.OnPreferenceStartFragmentCallback {

    private static final int BUY_REQUEST_CODE = 1001;

    private final DonateManager donateManager = new DonateManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        donateManager.init();
        setContentView(R.layout.a__settings);
        subscribeForEvents();
    }

    private void subscribeForEvents() {
        EventManager manager = App.get().getEventManager();
        manager.subscribe(this, EventType.REQUEST_LOAD_DONATE_PRODUCTS, new EventManager.OnEventListener<RequestLoadDonateProductsEvent>() {
            @Override
            public void onEvent(RequestLoadDonateProductsEvent event) {
                donateManager.loadProducts(new DonateManager.OnLoadProductsListener() {
                    @Override
                    public void onLoadProducts(List<DonateProduct> products) {
                        App.get().getEventManager().publish(new LoadDonateProductsEvent(products));
                    }
                });
            }
        });
        manager.subscribe(this, EventType.REQUEST_DONATE, new EventManager.OnEventListener<RequestDonateEvent>() {
            @Override
            public void onEvent(RequestDonateEvent event) {
                DonateProduct product = event.getProduct();
                PendingIntent intent = donateManager.getDonateIntent(product);
                if (intent == null) {
                    return;
                }
                try {
                    startIntentSenderForResult(intent.getIntentSender(), BUY_REQUEST_CODE, new Intent(), 0, 0, 0);
                    App.get().getAnalytics().reportEvent(Analytics.Category.DONATE, Analytics.Action.CLICK, product.getId());
                } catch (IntentSender.SendIntentException e) {

                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        App.get().getAnalytics().reportActivityStart(this);
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
        if (key.equals(SettingsFragment.PREF_KEY_RATE)) {
            goToMarket();
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
        if (manager.findFragmentByTag(FragmentTag.DONATE) == null) {
            (new DonateFragment()).show(manager, FragmentTag.DONATE);
            App.get().getAnalytics().reportEvent(Analytics.Category.DIALOGS, Analytics.Action.SHOW, "donate");
        }
    }

    private void goToMarket() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("market://details?id=%s", getPackageName())));
        startActivity(intent);
        App.get().getAnalytics().reportEvent(Analytics.Category.DIALOGS, Analytics.Action.SHOW, "rate");
    }

    private void showShareMessage() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.__share, getPackageName()));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {

        }
        App.get().getAnalytics().reportEvent(Analytics.Category.DIALOGS, Analytics.Action.SHOW, "share");
    }

    private void showAboutMessage() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentByTag(FragmentTag.ABOUT) == null) {
            (new AboutFragment()).show(manager, FragmentTag.ABOUT);
            App.get().getAnalytics().reportEvent(Analytics.Category.DIALOGS, Analytics.Action.SHOW, "about");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BUY_REQUEST_CODE && resultCode == RESULT_OK) {
            donateManager.handleDonate(data.getStringExtra("INAPP_PURCHASE_DATA"));
            App.get().getEventManager().publish(new DonateEvent());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        App.get().getAnalytics().reportActivityStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.get().getEventManager().unsubscribeAll(this);
        donateManager.deinit();
    }
}
