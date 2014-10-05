package com.micdm.transportlive.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import com.micdm.transportlive.data.ForecastVehicle;

public class ForecastVehicleParcel implements Parcelable {

    public static final Creator<ForecastVehicle> CREATOR = new Creator<ForecastVehicle>() {

        public ForecastVehicle createFromParcel(Parcel in) {
            int routeNumber = in.readInt();
            int arrivalTime = in.readInt();
            boolean isLowFloor = (in.readInt() == 1);
            return new ForecastVehicle(routeNumber, arrivalTime, isLowFloor);
        }

        public ForecastVehicle[] newArray(int size) {
            return new ForecastVehicle[size];
        }
    };

    private final ForecastVehicle vehicle;

    public ForecastVehicleParcel(ForecastVehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(vehicle.getRouteNumber());
        dest.writeInt(vehicle.getArrivalTime());
        dest.writeInt(vehicle.isLowFloor() ? 1 : 0);
    }

    public ForecastVehicle getVehicle() {
        return vehicle;
    }
}
