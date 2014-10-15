package com.micdm.transportlive.server.messages.outcoming;

import com.micdm.transportlive.server.messages.Message;

import java.math.BigDecimal;

public class LoadNearestStationsMessage implements Message {

    private final BigDecimal latitude;
    private final BigDecimal longitude;

    public LoadNearestStationsMessage(BigDecimal latitude, BigDecimal longitude) {
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
