package com.micdm.transportlive.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import com.micdm.transportlive.data.Vehicle;

import java.math.BigDecimal;

public class VehicleParcel implements Parcelable {

    public static final Creator<Vehicle> CREATOR = new Creator<Vehicle>() {

        public Vehicle createFromParcel(Parcel in) {
            String number = in.readString();
            BigDecimal latitude = new BigDecimal(in.readString());
            BigDecimal longitude = new BigDecimal(in.readString());
            int course = in.readInt();
            return new Vehicle(number, latitude, longitude, course);
        }

        public Vehicle[] newArray(int size) {
            return new Vehicle[size];
        }
    };

    private final Vehicle vehicle;

    public VehicleParcel(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vehicle.getNumber());
        dest.writeString(vehicle.getLatitude().toString());
        dest.writeString(vehicle.getLongitude().toString());
        dest.writeInt(vehicle.getCourse());
    }

    public Vehicle getVehicle() {
        return vehicle;
    }
}
