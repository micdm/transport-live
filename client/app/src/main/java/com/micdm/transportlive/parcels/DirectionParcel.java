package com.micdm.transportlive.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import com.micdm.transportlive.data.service.Direction;
import com.micdm.transportlive.data.service.Station;

import java.util.ArrayList;
import java.util.List;

public class DirectionParcel implements Parcelable {

    public static final Creator<Direction> CREATOR = new Creator<Direction>() {

        public Direction createFromParcel(Parcel in) {
            int id = in.readInt();
            List<Station> stations = new ArrayList<Station>();
            in.readTypedList(stations, StationParcel.CREATOR);
            return new Direction(id, stations);
        }

        public Direction[] newArray(int size) {
            return new Direction[size];
        }
    };

    private final Direction direction;

    public DirectionParcel(Direction direction) {
        this.direction = direction;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(direction.getId());
        dest.writeTypedList(getStationParcels());
    }

    private List<Parcelable> getStationParcels() {
        List<Parcelable> parcels = new ArrayList<Parcelable>();
        for (Station station: direction.getStations()) {
            parcels.add(new StationParcel(station));
        }
        return parcels;
    }

    public Direction getDirection() {
        return direction;
    }
}
