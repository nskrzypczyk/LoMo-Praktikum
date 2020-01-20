package com.example.praktikum1;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Prakt3Dist extends AppCompatActivity implements SensorEventListener{

    SeekBar slider;
    Button btnStart;
    private TextView tvLat, tvLong, tvAlt, tvInterval, tvTimestamp, tvCounter, tvSigMotion, tvTimestampLast, tvDist;
    LocationManager locManager;
    LocationListener locListener;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorListener;
    int red = Color.RED;
    int green = Color.GREEN;

    String firstTimestamp = "";
    @Getter
    OkHttpClient client;

    Location lastLoc = new Location("");
    boolean firstLoc = true;

    boolean sigMotion = true;
    Location mCurrentLocation;

    boolean GPSActive = false;
    boolean isActive;
    int dist = 1;
    int counter = 0;

    float lastVelocity = 0;
    float distanceTraveled = 0;
    int geschwindigkeit = 2; // in m/s

    long lastAccTime;
    boolean firstAcc = true;
    boolean secondAcc = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prakt3_dist);
        client = new OkHttpClient(); // http client instanz
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // locationmanager instanz
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        System.out.println(sensor.getName());
        sensorManager.registerListener(this, sensor , SensorManager.SENSOR_DELAY_NORMAL);
        this.initComponents();

        locListener = new android.location.LocationListener() {
            @Override // wird aufgerufen, wenn es Positionsupdates gibt
            public void onLocationChanged(Location location) {
                if (location == null) {
                    System.out.println("Keine Daten");
                }

                counter++;
                try {
                    String formattedDate = Utils.getTimeStamp(location);
                    tvTimestampLast.setText(formattedDate);
                    if(firstLoc){
                        POSTrequest(location);
                        lastLoc = location;
                        mCurrentLocation = location;
                        firstTimestamp = formattedDate;
                        lastVelocity = location.getSpeed();

                        updateUI();
                        stopGPS();
                        firstLoc = false;
                        sigMotion = false;
                        tvSigMotion.setTextColor(red);



                        /*
                        tvDist.setText("GPS auto. ausgeschaltet");
                        int secs = (dist/geschwindigkeit)-1;

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startGPS();
                            }
                        }, secs*1000);


                         */



                    }
                    //else if(true){
                    else if(sigMotion){
                        float distToLast = lastLoc.distanceTo(location);
                        float distVerg = dist;
                        tvDist.setText(distToLast + "");
                        sigMotion = false;
                        tvSigMotion.setTextColor(red);
                        if(lastLoc.distanceTo(location) >= distVerg){
                            lastLoc = location;
                            mCurrentLocation = location;
                            lastVelocity = location.getSpeed();

                            POSTrequest(location);
                            updateUI();




/*
                            tvDist.setText("GPS auto. ausgeschaltet");
                            int secs = (dist/geschwindigkeit)-1;

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startGPS();
                                }
                            }, secs*1000);


 */



                        }
                        stopGPS();
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

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor mySensor = sensorEvent.sensor;
        /*
        System.out.println("Listener: " + sensor.getName());
        if (mySensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            distanceTraveled += 0.7f;
            System.out.println(distanceTraveled);
            tvDist.setText(distanceTraveled + "");
            float distVerg = dist;
            if(distanceTraveled >= distVerg){
                distanceTraveled = 0;
            }
        }
         */


        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            if(x >= 5.0 || x <= -5.0 || y >= 5.0 || y <= -5.0 || z >= 5.0 || z <= -5.0  ){
                this.sigMotion = true;
                tvSigMotion.setTextColor(green);
                startGPS();
            }

            /*
            if(firstAcc){
                firstAcc = false;
                lastAccTime = System.currentTimeMillis();        }
            else{
                if(secondAcc){
                    secondAcc = false;
                    float time = (System.currentTimeMillis() - lastAccTime)/1000f;
                    lastAccTime = System.currentTimeMillis();
                    lastVelocity += y*time;
                }
                else{
                    float time = (System.currentTimeMillis() - lastAccTime)/1000f;
                    lastAccTime = System.currentTimeMillis();
                    if(lastVelocity > 0.1f || lastVelocity < -0.1f){
                        lastVelocity = 0;
                    }
                    distanceTraveled += lastVelocity*time;
                    float newVel = y*time;
                    if(newVel < 0.0f){
                        newVel = newVel * 0.7f;
                    }
                    lastVelocity += newVel;
                    tvDist.setText(distanceTraveled + "");
                }
            }

             */

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void initComponents() {
        this.tvLat = this.findViewById(R.id.tvLat);
        this.tvLong = this.findViewById(R.id.tvLong);
        this.tvAlt = this.findViewById(R.id.tvAlt);
        this.tvDist = this.findViewById(R.id.tvDist);
        this.tvInterval = this.findViewById(R.id.tvInterval);
        this.tvTimestamp = this.findViewById(R.id.tvTimestamp);
        this.tvTimestampLast = this.findViewById(R.id.tvTimestampLast);
        this.tvCounter = this.findViewById(R.id.tvCounter);
        this.tvSigMotion = this.findViewById(R.id.tvSigMotion);
        this.btnStart = this.findViewById(R.id.btnStart);
        this.slider = this.findViewById(R.id.slider);
        this.slider.setMin(1);
        this.slider.setMax(50);

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

    public void stopGPS() {
        GPSActive = false;
        locManager.removeUpdates(locListener);
    }

    private void stop(){
        stopGPS();
        tvLong.setText("STOP");
        tvLat.setText("STOP");
        tvAlt.setText("STOP");
        isActive = false;
        btnStart.setText("START");
    }

    private void start(){
        System.out.println("Start");
        btnStart.setText("STOP");
        isActive = true;
        startGPS();
    }

    private void startGPS(){
        if(!GPSActive) {
            System.out.println("GPS Active");
            GPSActive = true;
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locListener);
        }
    }

    public void POSTrequest(Location position) {
        if(!this.getURL().equals("")) {
            System.out.println("URL: " + "http://" + this.getURL());
            String URL = "http://" + this.getURL();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MediaType JSON = MediaType.parse("application/json;charset=utf-8");
                    JSONObject actual = new JSONObject();
                    RequestBody body = null;
                    try {
                        actual.put("text", firstTimestamp + "_" + counter);
                        actual.put("timeStamp", Utils.getTimeStamp(mCurrentLocation));
                        actual.put("lat", position.getLatitude());
                        actual.put("long", position.getLongitude());
                        actual.put("alt", position.getAltitude());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        body = RequestBody.create(JSON, actual.toString());
                        Request req = new Request.Builder().url(URL + "/api/position/send").post(body).build();
                        Response res = client.newCall(req).execute();
                        System.out.println(res.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println(body);
                }
            }).start();
        }
    }

    // Serveradresse aus der Settings XML holen
    public String getURL() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String val = sp.getString("server_address", "http://localhost:80");
        return val;
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

}
