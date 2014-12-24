package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

import java.math.BigDecimal;

public class UpdateLocationEvent extends Event {

    private final BigDecimal latitude;
    private final BigDecimal longitude;

    public UpdateLocationEvent(BigDecimal latitude, BigDecimal longitude) {
        super(EventType.UPDATE_LOCATION);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }
}
