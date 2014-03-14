package com.micdm.transportlive.data;

import java.util.ArrayList;
import java.util.List;

public class Service {

    public List<Transport> transports = new ArrayList<Transport>();

    public Transport getTransportById(int id) {
        for (Transport transport: transports) {
            if (transport.id == id) {
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
