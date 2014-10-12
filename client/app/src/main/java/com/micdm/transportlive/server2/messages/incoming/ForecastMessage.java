package com.micdm.transportlive.server2.messages.incoming;

import com.micdm.transportlive.server2.messages.Message;

import java.util.List;

public class ForecastMessage implements Message {

    public static class Vehicle {

        private final String number;
        private final int routeNumber;
        private final int arrivalTime;
        private final boolean isLowFloor;

        public Vehicle(String number, int routeNumber, int arrivalTime, boolean isLowFloor) {
            this.number = number;
            this.routeNumber = routeNumber;
            this.arrivalTime = arrivalTime;
            this.isLowFloor = isLowFloor;
        }

        public String getNumber() {
            return number;
        }

        public int getRouteNumber() {
            return routeNumber;
        }

        public int getArrivalTime() {
            return arrivalTime;
        }

        public boolean isLowFloor() {
            return isLowFloor;
        }
    }

    private final int transportId;
    private final int stationId;
    private final List<Vehicle> vehicles;

    public ForecastMessage(int transportId, int stationId, List<Vehicle> vehicles) {
        this.transportId = transportId;
        this.stationId = stationId;
        this.vehicles = vehicles;
    }

    public int getTransportId() {
        return transportId;
    }

    public int getStationId() {
        return stationId;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }
}
