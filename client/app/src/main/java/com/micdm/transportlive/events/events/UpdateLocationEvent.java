package com.micdm.transportlive.events.events;

import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventType;

import java.math.BigDecimal;

public class UpdateLocationEvent extends Event {

    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final float accuracy;

    public UpdateLocationEvent(BigDecimal latitude, BigDecimal longitude, float accuracy) {
        super(EventType.UPDATE_LOCATION);
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }
}
