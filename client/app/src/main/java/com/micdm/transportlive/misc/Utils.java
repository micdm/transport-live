package com.micdm.transportlive.misc;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Transport;

public class Utils {

    public static String getTransportName(Context context, Transport transport) {
        switch (transport.getType()) {
            case TROLLEYBUS:
                return context.getString(R.string.transport_type_trolleybus);
            case TRAM:
                return context.getString(R.string.transport_type_tram);
            default:
                throw new RuntimeException("unknown transport type");
        }
    }

    public static String getAppTitle(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            ApplicationInfo info = manager.getApplicationInfo(context.getPackageName(), 0);
            return (String) manager.getApplicationLabel(info);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("cannot get application title");
        }
    }

    public static String getAppVersion(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("cannot get application version");
        }
    }
}
