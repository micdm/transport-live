package com.micdm.transportlive.fragments;

import android.os.Bundle;
import android.preference.Preference;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.micdm.transportlive.App;
import com.micdm.transportlive.R;
import com.micdm.transportlive.donate.DonateProduct;
import com.micdm.transportlive.events.EventManager;
import com.micdm.transportlive.events.EventType;
import com.micdm.transportlive.events.events.LoadDonateProductsEvent;
import com.micdm.transportlive.events.events.RequestLoadDonateProductsEvent;

import java.util.List;

public class SettingsFragment extends PreferenceFragment {

    public static final String PREF_KEY_DONATE = "pref_donate";
    public static final String PREF_KEY_SHARE = "pref_share";
    public static final String PREF_KEY_ABOUT = "pref_about";

    private Preference donatePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        removeDonatePreference();
    }

    @Override
    public void onStart() {
        super.onStart();
        subscribeForEvents();
        requestForData();
    }

    private void subscribeForEvents() {
        EventManager manager = App.get().getEventManager();
        manager.subscribe(this, EventType.LOAD_DONATE_PRODUCTS, new EventManager.OnEventListener<LoadDonateProductsEvent>() {
            @Override
            public void onEvent(LoadDonateProductsEvent event) {
                List<DonateProduct> products = event.getProducts();
                if (products == null || products.isEmpty()) {
                    removeDonatePreference();
                } else {
                    addDonatePreference();
                }
            }
        });
    }

    private void requestForData() {
        EventManager manager = App.get().getEventManager();
        manager.publish(new RequestLoadDonateProductsEvent());
    }

    private void addDonatePreference() {
        if (donatePreference != null) {
            getPreferenceScreen().addPreference(donatePreference);
            donatePreference = null;
        }
    }

    private void removeDonatePreference() {
        if (donatePreference == null) {
            donatePreference = findPreference(PREF_KEY_DONATE);
            getPreferenceScreen().removePreference(donatePreference);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        App.get().getEventManager().unsubscribeAll(this);
    }
}
