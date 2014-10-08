package com.micdm.transportlive.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import com.micdm.transportlive.data.service.Route;
import com.micdm.transportlive.data.service.Station;
import com.micdm.transportlive.data.service.Transport;

import java.util.ArrayList;
import java.util.List;

public class TransportParcel implements Parcelable {

    public static final Creator<Transport> CREATOR = new Creator<Transport>() {

        public Transport createFromParcel(Parcel in) {
            int id = in.readInt();
            List<Station> stations = new ArrayList<Station>();
            in.readTypedList(stations, StationParcel.CREATOR);
            List<Route> routes = new ArrayList<Route>();
            in.readTypedList(routes, RouteParcel.CREATOR);
            return new Transport(id, stations, routes);
        }

        public Transport[] newArray(int size) {
            return new Transport[size];
        }
    };

    private final Transport transport;

    public TransportParcel(Transport transport) {
        this.transport = transport;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(transport.getId());
        dest.writeTypedList(getStationParcels());
        dest.writeTypedList(getRouteParcels());
    }

    private List<Parcelable> getStationParcels() {
        List<Parcelable> parcels = new ArrayList<Parcelable>();
        for (Station station: transport.getStations()) {
            parcels.add(new StationParcel(station));
        }
        return parcels;
    }

    private List<Parcelable> getRouteParcels() {
        List<Parcelable> parcels = new ArrayList<Parcelable>();
        for (Route route: transport.getRoutes()) {
            parcels.add(new RouteParcel(route));
        }
        return parcels;
    }

    public Transport getTransport() {
        return transport;
    }
}
