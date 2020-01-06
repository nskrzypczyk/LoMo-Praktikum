package com.example.praktikum1;

import androidx.appcompat.app.AppCompatActivity;

import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;

public class Prakt3Dist extends AppCompatActivity {

    SeekBar slider;
    Button btnStart;
    private TextView tvLat, tvLong, tvAlt, tvInterval, tvTimestamp, tvCounter;
    LocationManager locManager;
    LocationListener locListener;
    private SensorManager sensorManager;
    private Sensor sensor;
    private TriggerEventListener triggerEventListener;

    Location lastLoc = new Location("");
    boolean firstLoc = true;

    boolean sigMotion = true;
    Location mCurrentLocation;

    boolean isActive;
    int dist = 1;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prakt3_dist);
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // locationmanager instanz
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        this.initComponents();

        triggerEventListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {
                sigMotion = true;
                startGPS();
            }
        };

        locListener = new android.location.LocationListener() {
            @Override // wird aufgerufen, wenn es Positionsupdates gibt
            public void onLocationChanged(Location location) {
                if (location == null) {
                    System.out.println("Keine Daten");
                }
                try {
                    if(firstLoc){
                        lastLoc = location;
                        mCurrentLocation = location;
                        counter++;
                        updateUI();
                        stopGPS();
                        firstLoc = false;
                        sigMotion = false;
                    }
                    else{
                        if(lastLoc.distanceTo(location) >= dist){
                            mCurrentLocation = location;
                            counter++;
                            updateUI();
                            stopGPS();
                            sigMotion = false;
                        }
                    }

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

        btnStart.setOnClickListener(e -> {
            if (!isActive) {
                start();
            } else {
                stop();
            }

        });

        this.slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                updateInterval(progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                stop();
            }
        });

        sensorManager.requestTriggerSensor(triggerEventListener, sensor);
    }

    private void initComponents() {
        this.tvLat = this.findViewById(R.id.tvLat);
        this.tvLong = this.findViewById(R.id.tvLong);
        this.tvAlt = this.findViewById(R.id.tvAlt);
        this.tvInterval = this.findViewById(R.id.tvInterval);
        this.tvTimestamp = this.findViewById(R.id.tvTimestamp);
        this.tvCounter = this.findViewById(R.id.tvCounter);
        this.btnStart = this.findViewById(R.id.btnStart);
        this.slider = this.findViewById(R.id.slider);
        this.slider.setMin(1);
        this.slider.setMax(25);

    }

    private void updateUI() {
        if (null != mCurrentLocation) {
            tvLat.setText(mCurrentLocation.getLatitude() + "");
            tvLong.setText(mCurrentLocation.getLongitude() + "");
            tvAlt.setText(mCurrentLocation.getAltitude() + "");
            String formattedDate = Utils.getTimeStamp(mCurrentLocation);
            tvTimestamp.setText(formattedDate);
            tvCounter.setText(counter + "");
            Log.d("time", "onLocationChanged: " + formattedDate);
            // Positionsobjekt erstellen
        } else {
        }
    }

    private void updateInterval(int dist){
        this.dist = dist;
        tvInterval.setText(dist + "");
    }

    public void stopGPS(){
        locManager.removeUpdates(locListener);
    }

    private void stop(){
        locManager.removeUpdates(locListener);
        tvLong.setText("STOP");
        tvLat.setText("STOP");
        tvAlt.setText("STOP");
        isActive = false;
        btnStart.setText("START");
    }

    private void start(){
        btnStart.setText("STOP");
    }

    private void startGPS(){
        if(firstLoc){
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
            isActive = true;
        }
        else if(sigMotion) {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
            isActive = true;
        }
    }

}
