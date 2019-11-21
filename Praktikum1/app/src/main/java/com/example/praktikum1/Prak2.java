package com.example.praktikum1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import okhttp3.internal.Util;

public class Prak2 extends Activity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static String TAG = "PRAK2";
    private static final long INTERVAL = 3000;
    private static final long FASTEST_INTERVAL = INTERVAL/2;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String timeStamp;
    Position currentPos;

    // LOC MANAGER
    android.location.LocationListener locListener;
    LocationManager locManager;

    private boolean selectedLocManager = false;
    private String[] modes;
    private boolean isActive = false;
    private Spinner spRoutes, spProvider;
    private TextView tvLat, tvLong, tvAlt, tvTimestamp;
    private Button btnStart, btnTimestamp;


    public static String chosenRoute;
    public static String chosenProvider;

    static private File logFile=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prak2);
        modes = getResources().getStringArray(R.array.location_provider_prak2);
        this.initComponents();
        this.initListeners();
        this.setUpLocManager();
        this.checkWritePermission();
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

    }

    private void checkWritePermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
            }
        }
    }

    private void setUpLocManager() {
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locListener = new android.location.LocationListener() {
            @Override // wird aufgerufen, wenn es Positionsupdates gibt
            public void onLocationChanged(Location location) {
                if (location == null) {
                    System.out.println("Keine Daten");
                }
                try {
                    mCurrentLocation = location;
                    updateUI();

                } catch (Exception e) {
                    Log.e("OOF", "onLocationChanged: ", e.getCause());
                    tvLong.setText("ERROR");
                    tvLat.setText("ERROR");
                    tvAlt.setText("ERROR");
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 10);
            }
            return;
        }
    }

    private int getPrio() {
        if (modes == null) {
            modes = getResources().getStringArray(R.array.location_provider_prak2);
        }
        if (chosenProvider.equals(modes[0])) {
            selectedLocManager=false;
            return LocationRequest.PRIORITY_HIGH_ACCURACY;
        } else if (chosenProvider.equals(modes[1])) {
            selectedLocManager=false;
            return LocationRequest.PRIORITY_LOW_POWER;
        } else {
            this.onPause();
            selectedLocManager = true;
            return 0;
        }
    }

    // TODO: WERTE AUS DEN DROPDOWN LESEN
    protected void createLocationRequest() {
        try {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(getPrio());
            Log.i(TAG, "Derzeitiger Provider ist: " + getPrio());
        } catch (Exception e) {

        }
    }


    private void updateUI() {
        Log.d(TAG, "UI wird geupdated");
        if (null != mCurrentLocation) {
            tvLat.setText(mCurrentLocation.getLatitude() + "");
            tvLong.setText(mCurrentLocation.getLongitude() + "");
            tvAlt.setText(mCurrentLocation.getAltitude() + "");
            String formattedDate = Utils.getTimeStamp(mCurrentLocation);
            Log.d("time", "onLocationChanged: " + formattedDate);
            tvTimestamp.setText(formattedDate);
            // Positionsobjekt erstellen
            currentPos = new Position(formattedDate, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                    mCurrentLocation.getAltitude());
        } else {
            Log.d(TAG, "location ist null");
        }
    }


    private void initComponents() {
        this.spRoutes = this.findViewById(R.id.spRoutes);
        this.spProvider = findViewById(R.id.spProvider);
        this.tvLat = this.findViewById(R.id.tvLat);
        this.tvLong = this.findViewById(R.id.tvLong);
        this.tvAlt = this.findViewById(R.id.tvAlt);
        this.tvTimestamp = this.findViewById(R.id.tvTimestamp);
        this.btnStart = this.findViewById(R.id.btnStart);
        this.btnTimestamp = this.findViewById(R.id.btnTimestamp);
        selectedLocManager = false;
        chosenProvider = spProvider.getSelectedItem().toString();
    }

    private void initListeners() {
        spProvider.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                chosenProvider = parentView.getItemAtPosition(position).toString();
                getPrio();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Mach nix yo
            }

        });

        spRoutes.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                chosenRoute = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Mach nix yo
            }

        });

        btnStart.setOnClickListener(e -> {
            if (!isActive) {

                onStart();
                if(!selectedLocManager)
                    updateUI();
                btnStart.setText("STOP");
                isActive = true;
            } else {
                onPause();
                isActive = false;
                btnStart.setText("START");
            }

        });


    }

    public void onStart() {
        super.onStart();
        if (!selectedLocManager) {
            mGoogleApiClient.connect();
            if (mGoogleApiClient.isConnected()) {
                if(chosenProvider.equals(modes[0])){
                    logFile = Utils.Prak2LogFile(Utils.TYPE_FLP_HIGH,chosenRoute);
                }
                else if(chosenProvider.equals(modes[1])){
                    logFile = Utils.Prak2LogFile(Utils.TYPE_FLP_LOW,chosenRoute);
                }
                startLocationUpdates();
                Log.d(TAG, "Location updates laufen");
            }
        } else {
            Log.i(TAG, "Starte den Loc Manager mit GPS");
            logFile = Utils.Prak2LogFile(Utils.TYPE_LM_GPS, chosenRoute);
            checkLocationPermission();
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop gefeuert");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "ist connected!" + mGoogleApiClient.isConnected());
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected? -> " + mGoogleApiClient.isConnected());
        if(isActive && !chosenProvider.equals(modes[2])){
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        mGoogleApiClient.connect();
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location updates starten");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Verbindung fehlgeschlagen: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged gefeuert");
        mCurrentLocation = location;
        timeStamp = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        createLocationRequest();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }catch(Exception e){
            e.printStackTrace();
        }
            mGoogleApiClient.disconnect();
            Log.d(TAG, "Location updates gestoppt");

    }

    @Override
    public void onResume() {
        super.onResume();
    }




}
