package com.micdm.transportlive.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class GooglePlayLocator extends Locator {

    private final GooglePlayServicesClient.ConnectionCallbacks connectionCallbacks = new GooglePlayServicesClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            locationClient.requestLocationUpdates(locationRequest, locationListener);
        }
        @Override
        public void onDisconnected() {
            locationClient.removeLocationUpdates(locationListener);
        }
    };
    private final GooglePlayServicesClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }
    };
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            onUpdateLocationListener.onUpdateLocation(location);
        }
    };

    private final LocationRequest locationRequest;
    private final LocationClient locationClient;

    public GooglePlayLocator(Context context) {
        super(context);
        locationRequest = getLocationRequest();
        locationClient = getLocationClient();
    }

    private LocationRequest getLocationRequest() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(MIN_UPDATE_TIME * 1000);
        request.setFastestInterval(MIN_UPDATE_TIME / 2 * 1000);
        return request;
    }

    private LocationClient getLocationClient() {
        return new LocationClient(context, connectionCallbacks, onConnectionFailedListener);
    }

    @Override
    public void start() {
        locationClient.connect();
    }

    @Override
    public void stop() {
        locationClient.disconnect();
    }
}
