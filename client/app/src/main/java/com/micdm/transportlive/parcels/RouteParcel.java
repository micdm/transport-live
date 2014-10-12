package com.micdm.transportlive.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import com.micdm.transportlive.data.service.Direction;
import com.micdm.transportlive.data.service.Route;

import java.util.ArrayList;
import java.util.List;

public class RouteParcel implements Parcelable {

    public static final Creator<Route> CREATOR = new Creator<Route>() {

        public Route createFromParcel(Parcel in) {
            int number = in.readInt();
            List<Direction> directions = new ArrayList<Direction>();
            in.readTypedList(directions, DirectionParcel.CREATOR);
            return new Route(number, directions);
        }

        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    private final Route route;

    public RouteParcel(Route route) {
        this.route = route;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(route.getNumber());
        dest.writeTypedList(getDirectionParcels());
    }

    private List<Parcelable> getDirectionParcels() {
        List<Parcelable> parcels = new ArrayList<Parcelable>();
        for (Direction direction: route.getDirections()) {
            parcels.add(new DirectionParcel(direction));
        }
        return parcels;
    }
}
