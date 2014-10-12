package com.micdm.transportlive.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import com.micdm.transportlive.data.service.Station;

public class StationParcel implements Parcelable {

    public static final Creator<Station> CREATOR = new Creator<Station>() {

        public Station createFromParcel(Parcel in) {
            int id = in.readInt();
            String name = in.readString();
            return new Station(id, name);
        }

        public Station[] newArray(int size) {
            return new Station[size];
        }
    };

    private final Station station;

    public StationParcel(Station station) {
        this.station = station;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(station.getId());
        dest.writeString(station.getName());
    }
}
