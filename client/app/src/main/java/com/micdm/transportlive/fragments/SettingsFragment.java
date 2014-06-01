package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;

import com.micdm.transportlive.R;
import com.micdm.transportlive.donate.DonateItem;
import com.micdm.transportlive.interfaces.DonateHandler;

import java.util.List;

public class SettingsFragment extends PreferenceFragment {

    public static final String PREF_KEY_DONATE = "pref_donate";
    public static final String PREF_KEY_SHARE = "pref_share";
    public static final String PREF_KEY_ABOUT = "pref_about";

    private DonateHandler handler;
    private final DonateHandler.OnLoadDonateItemsListener onLoadDonateItemsListener = new DonateHandler.OnLoadDonateItemsListener() {
        @Override
        public void onLoadDonateItems(List<DonateItem> items) {
            if (items == null) {
                if (donatePreference == null) {
                    donatePreference = findPreference(PREF_KEY_DONATE);
                    getPreferenceScreen().removePreference(donatePreference);
                }
            } else {
                if (donatePreference != null) {
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
        handler.addOnLoadDonateItemsListener(onLoadDonateItemsListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeOnLoadDonateItemsListener(onLoadDonateItemsListener);
    }
}
