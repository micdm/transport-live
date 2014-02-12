package com.micdm.transportlive.data;

import java.util.ArrayList;

public class Service {

    public ArrayList<Transport> transports = new ArrayList<Transport>();

    public Transport getTransportByCode(String code) {
        for (Transport transport: transports) {
            if (transport.code.equals(code)) {
                return transport;
            }
        }
        return null;
    }
}
