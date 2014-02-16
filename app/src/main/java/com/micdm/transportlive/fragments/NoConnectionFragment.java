package com.micdm.transportlive.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.micdm.transportlive.R;

public class NoConnectionFragment extends DialogFragment {

    public static interface OnRetryConnectionListener {
        public void onRetryConnection();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setMessage(R.string.no_connection);
        builder.setPositiveButton(R.string.retry_connection, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                ((OnRetryConnectionListener) getActivity()).onRetryConnection();
            }
        });
        return builder.create();
    }
}
