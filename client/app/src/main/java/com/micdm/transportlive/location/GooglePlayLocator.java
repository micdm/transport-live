package com.micdm.transportlive.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GooglePlayLocator extends Locator {

    private final GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, locationListener);
        }
        @Override
        public void onConnectionSuspended(int reason) {

        }
    };
    private final GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
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
    private final GoogleApiClient apiClient;

    public GooglePlayLocator(Context context) {
        super(context);
        locationRequest = getLocationRequest();
        apiClient = getApiClient(context);
    }

    private LocationRequest getLocationRequest() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(MIN_UPDATE_TIME * 1000);
        request.setFastestInterval(MIN_UPDATE_TIME / 2 * 1000);
        return request;
    }

    private GoogleApiClient getApiClient(Context context) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context);
        builder.addApi(LocationServices.API);
        builder.addConnectionCallbacks(connectionCallbacks);
        builder.addOnConnectionFailedListener(onConnectionFailedListener);
        return builder.build();
    }

    @Override
    public void start() {
        apiClient.connect();
    }

    @Override
    public void stop() {
        if (apiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, locationListener);
            apiClient.disconnect();
        }
    }
}
