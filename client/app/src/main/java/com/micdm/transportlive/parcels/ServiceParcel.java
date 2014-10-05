package com.micdm.transportlive.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;

import java.util.ArrayList;
import java.util.List;

public class ServiceParcel implements Parcelable {

    public static final Creator<Service> CREATOR = new Creator<Service>() {

        public Service createFromParcel(Parcel in) {
            List<Transport> transports = new ArrayList<Transport>();
            in.readTypedList(transports, TransportParcel.CREATOR);
            return new Service(transports);
        }

        public Service[] newArray(int size) {
            return new Service[size];
        }
    };

    private final Service service;

    public ServiceParcel(Service service) {
        this.service = service;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(getTransportParcels());
    }

    private List<Parcelable> getTransportParcels() {
        List<Parcelable> parcels = new ArrayList<Parcelable>();
        for (Transport transport: service.getTransports()) {
            parcels.add(new TransportParcel(transport));
        }
        return parcels;
    }

    public Service getService() {
        return service;
    }
}
