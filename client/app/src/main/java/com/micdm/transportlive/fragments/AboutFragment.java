package com.micdm.transportlive.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.micdm.transportlive.R;
import com.micdm.transportlive.misc.Utils;

public class AboutFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.fragment_about_title);
        builder.setView(getContent());
        return builder.create();
    }

    private View getContent() {
        TextView view = (TextView) View.inflate(getActivity(), R.layout.f__about, null);
        String title = Utils.getAppTitle(getActivity());
        String version = Utils.getAppVersion(getActivity());
        String text = getString(R.string.fragment_about_text, title, version, getString(R.string.contact_email));
        view.setText(Html.fromHtml(text));
        return view;
    }
}
