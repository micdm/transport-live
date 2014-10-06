package com.micdm.transportlive.server2.messages.incoming;

import com.micdm.transportlive.server2.messages.Message;

import java.math.BigDecimal;

public class VehicleMessage implements Message {

    private final int transportId;
    private final int routeNumber;
    private final String number;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final int course;

    public VehicleMessage(int transportId, int routeNumber, String number, BigDecimal latitude, BigDecimal longitude, int course) {
        this.transportId = transportId;
        this.routeNumber = routeNumber;
        this.number = number;
        this.latitude = latitude;
        this.longitude = longitude;
        this.course = course;
    }

    public int getTransportId() {
        return transportId;
    }

    public int getRouteNumber() {
        return routeNumber;
    }

    public String getNumber() {
        return number;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public int getCourse() {
        return course;
    }
}
