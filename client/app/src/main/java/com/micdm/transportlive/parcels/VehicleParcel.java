package com.micdm.transportlive.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import com.micdm.transportlive.data.MapVehicle;

import java.math.BigDecimal;

public class VehicleParcel implements Parcelable {

    public static final Creator<MapVehicle> CREATOR = new Creator<MapVehicle>() {

        public MapVehicle createFromParcel(Parcel in) {
            String number = in.readString();
            int transportId = in.readInt();
            int routeNumber = in.readInt();
            BigDecimal latitude = new BigDecimal(in.readString());
            BigDecimal longitude = new BigDecimal(in.readString());
            int course = in.readInt();
            return new MapVehicle(number, transportId, routeNumber, latitude, longitude, course);
        }

        public MapVehicle[] newArray(int size) {
            return new MapVehicle[size];
        }
    };

    private final MapVehicle vehicle;

    public VehicleParcel(MapVehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vehicle.getNumber());
        dest.writeInt(vehicle.getTransportId());
        dest.writeInt(vehicle.getRouteNumber());
        dest.writeString(vehicle.getLatitude().toString());
        dest.writeString(vehicle.getLongitude().toString());
        dest.writeInt(vehicle.getCourse());
    }

    public MapVehicle getVehicle() {
        return vehicle;
    }
}
