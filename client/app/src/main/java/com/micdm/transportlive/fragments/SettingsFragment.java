package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.micdm.transportlive.R;
import com.micdm.transportlive.donate.DonateProduct;
import com.micdm.transportlive.interfaces.DonateHandler;

import java.util.List;

public class SettingsFragment extends PreferenceFragment {

    public static final String PREF_KEY_DONATE = "pref_donate";
    public static final String PREF_KEY_SHARE = "pref_share";
    public static final String PREF_KEY_ABOUT = "pref_about";

    private DonateHandler handler;
    private final DonateHandler.OnLoadDonateProductsListener onLoadDonateProductsListener = new DonateHandler.OnLoadDonateProductsListener() {
        @Override
        public void onLoadDonateProducts(List<DonateProduct> products) {
            if (products == null) {
                if (donatePreference == null) {
                    donatePreference = findPreference(PREF_KEY_DONATE);
                    getPreferenceScreen().removePreference(donatePreference);
                }
            } else {
                if (products.size() != 0 && donatePreference != null) {
                    getPreferenceScreen().addPreference(donatePreference);
                    donatePreference = null;
                }
            }
        }
    };
    private Preference donatePreference;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        handler = (DonateHandler) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onStart() {
        super.onStart();
        handler.addOnLoadDonateProductsListener(onLoadDonateProductsListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeOnLoadDonateProductsListener(onLoadDonateProductsListener);
    }
}
