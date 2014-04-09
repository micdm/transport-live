package com.micdm.transportlive.fragments;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;

import com.micdm.transportlive.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
