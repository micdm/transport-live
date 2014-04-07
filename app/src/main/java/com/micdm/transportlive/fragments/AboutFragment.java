package com.micdm.transportlive.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import com.micdm.transportlive.R;

public class AboutFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(getContent());
        return builder.create();
    }

    private View getContent() {
        TextView view = (TextView) View.inflate(getActivity(), R.layout.fragment_about, null);
        view.setText(getString(R.string.fragment_about_text, getString(R.string.contact_email)));
        return view;
    }
}
