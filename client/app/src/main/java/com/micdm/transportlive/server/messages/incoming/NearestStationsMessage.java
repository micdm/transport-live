package com.micdm.transportlive.server.messages.incoming;

import com.micdm.transportlive.server.messages.Message;

import java.util.List;

public class NearestStationsMessage implements Message {

    public static class Station {

        private final int transportId;
        private final int stationId;

        public Station(int transportId, int stationId) {
            this.transportId = transportId;
            this.stationId = stationId;
        }

        public int getTransportId() {
            return transportId;
        }

        public int getStationId() {
            return stationId;
        }
    }

    private final List<Station> stations;

    public NearestStationsMessage(List<Station> stations) {
        this.stations = stations;
    }

    public List<Station> getStations() {
        return stations;
    }
}
