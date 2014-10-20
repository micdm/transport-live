package com.micdm.transportlive.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import com.micdm.transportlive.data.SelectedStation;

public class SelectedStationParcel implements Parcelable {

    public static final Creator<SelectedStation> CREATOR = new Creator<SelectedStation>() {

        public SelectedStation createFromParcel(Parcel in) {
            int transportId = in.readInt();
            int routeNumber = in.readInt();
            int directionId = in.readInt();
            int stationId = in.readInt();
            boolean isFavourite = (in.readInt() == 1);
            return new SelectedStation(transportId, routeNumber, directionId, stationId, isFavourite);
        }

        public SelectedStation[] newArray(int size) {
            return new SelectedStation[size];
        }
    };

    private final SelectedStation station;

    public SelectedStationParcel(SelectedStation station) {
        this.station = station;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(station.getTransportId());
        dest.writeInt(station.getRouteNumber());
        dest.writeInt(station.getDirectionId());
        dest.writeInt(station.getStationId());
        dest.writeInt(station.isFavourite() ? 1 : 0);
    }

    public SelectedStation getStation() {
        return station;
    }
}
