package com.micdm.transportlive.data;

import java.util.ArrayList;

public class Service {

    public ArrayList<Transport> transports = new ArrayList<Transport>();

    public Transport getTransportById(int id) {
        for (Transport transport: transports) {
            if (transport.id == id) {
                return transport;
            }
        }
        return null;
    }

    public Transport getTransportByType(Transport.Type type) {
        for (Transport transport: transports) {
            if (transport.type.equals(type)) {
                return transport;
            }
        }
        return null;
    }

    public Transport getTransportByCode(String code) {
        for (Transport transport: transports) {
            if (transport.code.equals(code)) {
                return transport;
            }
        }
        return null;
    }
}
