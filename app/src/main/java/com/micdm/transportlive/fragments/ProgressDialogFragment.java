package com.micdm.transportlive.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog.Builder builder = new ProgressDialog.Builder(getActivity());
        builder.setMessage("Загружаем маршруты...");
        builder.setCancelable(false);
        return builder.create();
    }
}
