package com.micdm.transportlive.data.service;

import java.math.BigDecimal;

public class Point {

    private final BigDecimal latitude;
    private final BigDecimal longitude;

    public Point(BigDecimal latitude, BigDecimal longitude) {
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
