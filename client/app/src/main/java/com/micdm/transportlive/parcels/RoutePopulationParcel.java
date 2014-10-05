package com.micdm.transportlive.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import com.micdm.transportlive.data.RoutePopulation;
import com.micdm.transportlive.data.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class RoutePopulationParcel implements Parcelable {

    public static final Creator<RoutePopulation> CREATOR = new Creator<RoutePopulation>() {

        public RoutePopulation createFromParcel(Parcel in) {
            int transportId = in.readInt();
            int routeNumber = in.readInt();
            List<Vehicle> vehicles = new ArrayList<Vehicle>();
            in.readTypedList(vehicles, VehicleParcel.CREATOR);
            return new RoutePopulation(transportId, routeNumber, vehicles);
        }

        public RoutePopulation[] newArray(int size) {
            return new RoutePopulation[size];
        }
    };

    private final RoutePopulation population;

    public RoutePopulationParcel(RoutePopulation population) {
        this.population = population;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(population.getTransportId());
        dest.writeInt(population.getRouteNumber());
        dest.writeTypedList(getVehicleParcels());
    }

    private List<Parcelable> getVehicleParcels() {
        List<Parcelable> parcels = new ArrayList<Parcelable>();
        for (Vehicle vehicle: population.getVehicles()) {
            parcels.add(new VehicleParcel(vehicle));
        }
        return parcels;
    }

    public RoutePopulation getPopulation() {
        return population;
    }
}
