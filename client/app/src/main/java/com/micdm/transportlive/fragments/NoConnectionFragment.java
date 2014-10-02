package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.micdm.transportlive.R;
import com.micdm.transportlive.interfaces.ConnectionHandler;

public class NoConnectionFragment extends DialogFragment {

    private ConnectionHandler handler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        handler = (ConnectionHandler) activity;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.f__no_connection__title);
        builder.setView(View.inflate(getActivity(), R.layout.f__no_connection, null));
        builder.setPositiveButton(R.string.f__no_connection__retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        setCancelable(false);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        handler.requestReconnect();
    }
}
