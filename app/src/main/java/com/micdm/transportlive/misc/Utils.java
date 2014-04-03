package com.micdm.transportlive.misc;

import android.content.Context;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Transport;

public class Utils {

    public static String getTransportName(Context context, Transport transport) {
        switch (transport.type) {
            case BUS:
                return context.getString(R.string.transport_type_bus);
            case TROLLEYBUS:
                return context.getString(R.string.transport_type_trolleybus);
            case TRAM:
                return context.getString(R.string.transport_type_tram);
            case TAXI:
                return context.getString(R.string.transport_type_taxi);
            default:
                throw new RuntimeException("unknown transport type");
        }
    }
}
