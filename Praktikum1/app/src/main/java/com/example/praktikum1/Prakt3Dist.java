package com.example.praktikum1;

import androidx.appcompat.app.AppCompatActivity;

import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import org.json.JSONObject;

import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Prakt3Dist extends AppCompatActivity {

    SeekBar slider;
    Button btnStart;
    private TextView tvLat, tvLong, tvAlt, tvInterval, tvTimestamp, tvCounter, tvSigMotion;
    LocationManager locManager;
    LocationListener locListener;
    private SensorManager sensorManager;
    private Sensor sensor;
    private TriggerEventListener triggerEventListener;
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
    long nextPosTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prakt3_dist);
        client = new OkHttpClient(); // http client instanz
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // locationmanager instanz
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        this.initComponents();

        triggerEventListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {
                System.out.println("SigMotion detected!");
                if(isActive) {
                    sigMotion = true;
                    tvSigMotion.setTextColor(green);
                    startGPS();
                }
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
                        float eModel = location.getAccuracy();
                        float vEst = location.getSpeed();
                        float tLimit;
                        try {
                            tLimit = (dist - eModel) / vEst;
                        }
                        catch(Exception e){
                            tLimit = 1;
                        }
                        int sec = Math.round(tLimit);
                        nextPosTime = System.currentTimeMillis() + sec;

                        counter++;
                        updateUI();
                        stopGPS();
                        firstLoc = false;
                        sigMotion = false;
                        tvSigMotion.setTextColor(red);
                    }
                    else{
                        if(lastLoc.distanceTo(location) >= dist && nextPosTime <= System.currentTimeMillis()){
                            mCurrentLocation = location;
                            float eModel = location.getAccuracy();
                            float vEst = location.getSpeed();
                            float tLimit = (dist-eModel)/vEst;
                            int sec = Math.round(tLimit);
                            nextPosTime = System.currentTimeMillis() + sec;
                            counter++;
                            updateUI();
                            stopGPS();
                            sigMotion = false;
                            tvSigMotion.setTextColor(red);
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
        this.tvSigMotion = this.findViewById(R.id.tvSigMotion);
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

    public void stopGPS() {
        GPSActive = false;
        locManager.removeUpdates(locListener);
    }

    private void stop(){
        GPSActive = false;
        locManager.removeUpdates(locListener);
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
        if(firstLoc){
            System.out.println("GPS Active");
            GPSActive = true;
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);

        }
        else if(sigMotion && !GPSActive) {
            System.out.println("GPS Active");
            GPSActive = true;
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
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

}
