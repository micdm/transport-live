package com.micdm.transportlive.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import com.micdm.transportlive.data.SelectedRoute;

public class SelectedRouteParcel implements Parcelable {

    public static final Creator<SelectedRoute> CREATOR = new Creator<SelectedRoute>() {

        public SelectedRoute createFromParcel(Parcel in) {
            int transportId = in.readInt();
            int routeNumber = in.readInt();
            return new SelectedRoute(transportId, routeNumber);
        }

        public SelectedRoute[] newArray(int size) {
            return new SelectedRoute[size];
        }
    };

    private final SelectedRoute route;

    public SelectedRouteParcel(SelectedRoute route) {
        this.route = route;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(route.getTransportId());
        dest.writeInt(route.getRouteNumber());
    }

    public SelectedRoute getRoute() {
        return route;
    }
}
