package com.example.praktikum1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    Button btnUpdate;
    TextView tvGPSLong, tvGPSLat;
    LocationManager locManager;
    LocationListener locListener;
    Location locGPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        btnUpdate = findViewById(R.id.btnUpdate);
        //btnUpdate.setOnClickListener(e -> this.getLocationData());
        tvGPSLong = (TextView) findViewById(R.id.tvGPSLong);
        tvGPSLat = (TextView) findViewById(R.id.tvGPSLat);
        locListener = new LocationListener() {
            @Override //wird aufgerufen, wenn es Positionsupdates gibt
            public void onLocationChanged(Location location) {
                if(location==null){
                    System.out.println("S.H.I.T.");
                }
                try {
                    tvGPSLat.setText(location.getLatitude() + "");
                    tvGPSLong.setText(location.getLongitude() + "");
                } catch (Exception e) {
                    Log.e("OOF", "onLocationChanged: ", e.getCause());
                    tvGPSLat.setText("ERROR");
                    tvGPSLong.setText("ERROR");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override //checkt ob Standort aus ist
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        configureButton();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                configureButton();
                break;
            default:
                break;
        }
    }

    public void getLocationData() {


    }

    private void configureButton() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        btnUpdate.setOnClickListener(e->{
                locManager.requestLocationUpdates("gps", 5000, 0, locListener);
        });
    }

}
