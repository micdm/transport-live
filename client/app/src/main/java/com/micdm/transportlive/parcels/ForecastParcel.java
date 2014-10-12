package com.micdm.transportlive.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.ForecastVehicle;

import java.util.ArrayList;
import java.util.List;

public class ForecastParcel implements Parcelable {

    public static final Creator<Forecast> CREATOR = new Creator<Forecast>() {

        public Forecast createFromParcel(Parcel in) {
            int transportId = in.readInt();
            int stationId = in.readInt();
            List<ForecastVehicle> vehicles = new ArrayList<ForecastVehicle>();
            in.readTypedList(vehicles, ForecastVehicleParcel.CREATOR);
            return new Forecast(transportId, stationId, vehicles);
        }

        public Forecast[] newArray(int size) {
            return new Forecast[size];
        }
    };

    private final Forecast forecast;

    public ForecastParcel(Forecast forecast) {
        this.forecast = forecast;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(forecast.getTransportId());
        dest.writeInt(forecast.getStationId());
        dest.writeTypedList(getForecastVehicleParcels());
    }

    private List<Parcelable> getForecastVehicleParcels() {
        List<Parcelable> parcels = new ArrayList<Parcelable>();
        for (ForecastVehicle vehicle: forecast.getVehicles()) {
            parcels.add(new ForecastVehicleParcel(vehicle));
        }
        return parcels;
    }

    public Forecast getForecast() {
        return forecast;
    }
}
