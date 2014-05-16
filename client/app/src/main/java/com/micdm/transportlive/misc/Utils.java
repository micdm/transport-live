package com.micdm.transportlive.misc;

import android.content.Context;

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
}
