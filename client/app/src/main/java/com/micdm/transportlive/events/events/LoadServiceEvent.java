package com.micdm.transportlive.events.events;

import com.micdm.transportlive.data.service.Service;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

public class LoadServiceEvent extends Event {

    private final Service service;

    public LoadServiceEvent(Service service) {
        super(EventType.LOAD_SERVICE);
        this.service = service;
    }

    public Service getService() {
        return service;
    }
}
