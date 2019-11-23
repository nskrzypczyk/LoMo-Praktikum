package com.example.praktikum1;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.*;

import java.util.Calendar;


public class FusedLocation implements ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener {


    public static final long UPDATE_INTERVALL = 3000;

    public static final long SCHNELLERES_INTERVALL = UPDATE_INTERVALL / 2;
    private final static String TAG = "FUSED LOCATION";

    public Callback mCallback = null;

    // Parameter für die FLP API
    protected LocationRequest mLocationRequest;

    // Play Services Gedöns
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private Location mCurrentLocation = null;
    private int PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    private boolean inProgress = false;
    private int numTries = 0;
    private long diffTime = 5000;
    private float minAccuracy = 35;
    private int maxTries = 1;
    private boolean lastKnownLocation = false;
    Resources res = MainActivity.getInstance().getResources();
    String[] providers = res.getStringArray(R.array.location_provider_prak2);

    public FusedLocation(Context c, Callback callback) {
        this.mContext = c;
        this.mCallback = callback;
        this.getCurrentLocation();
    }
    
    /**
      setzt die FLP Loc updates auf
    */
    public void getCurrentLocation() {
        chooseNetworkGps();
        buildGoogleApiClient();
        lastKnownLocation = false;
        inProgress = true;
        if (canGetLocation())
            mGoogleApiClient.connect();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Baue den GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVALL);
        mLocationRequest.setFastestInterval(SCHNELLERES_INTERVALL);
        mLocationRequest.setPriority(PRIORITY);
    }

    /**
     * Startet die location updates
     */
    public void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Beendet die Location updates
     */
    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        reset();
    }

    @Override
    public void onLocationChanged(Location location) {
        chooseNetworkGps();
        numTries++;
        if (mCurrentLocation == null)
            mCurrentLocation = location;
        else if (mCurrentLocation.getAccuracy() > location.getAccuracy()) {
            mCurrentLocation = location;
        }
        if (numTries >= maxTries) {
            mCallback.onLocationResult(mCurrentLocation);
            stopLocationUpdates();
        }

    }

    public Location getLocation(int maxtries) {
        if (numTries >= maxtries)
            return mCurrentLocation;
        else
            return null;
    }

    /**
     * Ausgeführt wenn ein Api client objekt sich erfolgreich verbindet
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Verbunden mit API Client");
        if (lastKnownLocation) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mCurrentLocation != null && (mCurrentLocation.getTime() - Calendar.getInstance().getTime().getTime()) < diffTime
                    && mCurrentLocation.getAccuracy() <= minAccuracy) {
                mCallback.onLocationResult(mCurrentLocation);
                reset();
            } else {
                startLocationUpdates();
            }
        } else {
            startLocationUpdates();
        }

    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Verbindung wurde suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Verbindung fehlgeschlagen; Errorcode: " + result.getErrorCode());
    }


    public void reset() {
        numTries = 0;
        mCurrentLocation = null;
        inProgress = false;
        lastKnownLocation = false;
        mGoogleApiClient.disconnect();
    }

    public boolean canGetLocation() {
        return isNetworkEnabled() || isGPSEnabled();
    }

    public boolean isNetworkEnabled() {
        return ((LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public boolean isGPSEnabled() {
        return ((LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void chooseNetworkGps() {
        if (Prak2.chosenProvider.equals(providers[0])) {
            PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
        } else if (Prak2.chosenProvider.equals(providers[1])) {
            PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
        }
        else {
            PRIORITY = LocationRequest.PRIORITY_NO_POWER;
        }
    }

    interface Callback {
        public void onLocationResult(Location location);
    }
}
