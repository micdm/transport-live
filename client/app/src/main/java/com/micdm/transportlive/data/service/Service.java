package com.micdm.transportlive.data.service;

import java.util.List;

public class Service {

    private final List<Transport> transports;

    public Service(List<Transport> transports) {
        this.transports = transports;
    }

    public List<Transport> getTransports() {
        return transports;
    }

    public Transport getTransportById(int id) {
        for (Transport transport: transports) {
            if (transport.getId() == id) {
                return transport;
            }
        }
        return null;
    }
}
