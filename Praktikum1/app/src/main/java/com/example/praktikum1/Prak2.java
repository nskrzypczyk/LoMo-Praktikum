package com.example.praktikum1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class Prak2 extends Activity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static String TAG = "PRAK2";
    private static final long INTERVAL = 3000;
    private static final long FASTEST_INTERVAL = 3000;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String timeStamp;


    private Spinner spRoutes, spProvider;
    private TextView tvLat, tvLong;
    private Button btnStart, btnTimestamp;

    public static String chosenRoute;
    public static String chosenProvider;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prak2);

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

        this.initComponents();
        this.initListeners();
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

    // TODO: WERTE AUS DEN DROPDOWN LESEN
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }



    private void updateUI() {
        Log.d(TAG, "UI wird geupdated");
        if (null != mCurrentLocation) {
//            String lat = String.valueOf(mCurrentLocation.getLatitude());
//            String lng = String.valueOf(mCurrentLocation.getLongitude());
//            tvLocation.setText("At Time: " + mLastUpdateTime + "\n" +
//                    "Latitude: " + lat + "\n" +
//                    "Longitude: " + lng + "\n" +
//                    "Accuracy: " + mCurrentLocation.getAccuracy() + "\n" +
//                    "Provider: " + mCurrentLocation.getProvider());
            tvLat.setText(mCurrentLocation.getLatitude()+"");
            tvLong.setText(mCurrentLocation.getLongitude()+"");
        } else {
            Log.d(TAG, "location ist null");
        }
    }



    private void initComponents(){
        this.spRoutes=findViewById(R.id.spRoutes);
        this.spProvider=findViewById(R.id.spProvider);
        this.tvLat = findViewById(R.id.tvLat);
        this.tvLong =findViewById(R.id.tvLong);
        this.btnStart = findViewById(R.id.btnStart);
        this.btnTimestamp = findViewById(R.id.btnTimestamp);

        chosenProvider = spProvider.getSelectedItem().toString();
    }

    private void initListeners() {
        spProvider.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        btnStart.setOnClickListener(e-> {
            onStart();
            updateUI();

        });


    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location updates laufen");
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
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        Log.d(TAG, "Location updates gestoppt");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location updates laufen weiter");
        }
    }


}
