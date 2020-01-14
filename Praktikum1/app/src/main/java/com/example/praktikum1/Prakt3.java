package com.example.praktikum1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class Prakt3 extends AppCompatActivity {

    SeekBar slider;
    Button btnStart, btnDist, btnExport;
    private TextView tvLat, tvLong, tvAlt, tvInterval, tvTimestamp, tvCounter;
    LocationManager locManager;
    LocationListener locListener;
    String firstTimestamp = "";

    Location mCurrentLocation;

    @Getter
    OkHttpClient client;

    boolean isActive;
    int interval = 1;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prakt3);
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // locationmanager instanz
        this.initComponents();
        client = new OkHttpClient(); // http client instanz

        locListener = new android.location.LocationListener() {
            @Override // wird aufgerufen, wenn es Positionsupdates gibt
            public void onLocationChanged(Location location) {
                if (location == null) {
                    System.out.println("Keine Daten");
                }
                try {
                    mCurrentLocation = location;
                    if(firstTimestamp.equals("")){
                        firstTimestamp = Utils.getTimeStamp(mCurrentLocation);
                    }
                    counter++;
                    updateUI();

                } catch (Exception e) {
                    Log.e("OOF", "onLocationChanged: ", e.getCause());
                    tvLong.setText("ERROR");
                    tvLat.setText("ERROR");
                    tvAlt.setText("ERROR");
                }

                try {
                    POSTrequest(location);
                } catch (Exception e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(Prakt3.this).setTitle("Ein Fehler ist aufgetreten")
                            .setMessage("Der angegebene Server konnte nicht erreicht werden :(")
                            .setNeutralButton("Serverangabe prüfen", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Prakt3.this, SettingsActivity.class);
                                    startActivity(intent);
                                }
                            }).create().show(); // TODO: Wird noch nicht angezeigt
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

        btnExport.setOnClickListener(e -> {
            this.Exportrequest();
        });

        btnDist.setOnClickListener(e->{
            Intent i = new Intent(this, Prakt3Dist.class);
            startActivity(i);
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

    private void initComponents() {
        this.tvLat = this.findViewById(R.id.tvLat);
        this.tvLong = this.findViewById(R.id.tvLong);
        this.tvAlt = this.findViewById(R.id.tvAlt);
        this.tvInterval = this.findViewById(R.id.tvInterval);
        this.tvTimestamp = this.findViewById(R.id.tvTimestamp);
        this.tvCounter = this.findViewById(R.id.tvCounter);
        this.btnStart = this.findViewById(R.id.btnStart);
        this.btnDist = this.findViewById(R.id.btnDist);
        this.btnExport = this.findViewById(R.id.export);
        this.slider = this.findViewById(R.id.slider);
        this.slider.setMin(1);
        this.slider.setMax(10);

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

    private void updateInterval(int interval){
        this.interval = interval;
        tvInterval.setText(interval + "");
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

        System.out.println("GPS gestartet: Interval: " + this.interval);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, this.interval*1000, 0, locListener);
        isActive = true;
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

    public void Exportrequest() {
        System.out.println("URL: " + "http://" + this.getURL());
        String URL = "http://" + this.getURL();
        new Thread(new Runnable() {
            @Override
            public void run() {
                MediaType JSON = MediaType.parse("application/json;charset=utf-8");
                JSONObject actual = new JSONObject();
                RequestBody body = null;
                try {
                    body = RequestBody.create(JSON, actual.toString());
                    Request req = new Request.Builder().url(URL + "/api/position/export").post(body).build();
                    Response res = client.newCall(req).execute();
                    System.out.println(res.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(body);
            }
        }).start();
    }

    // Serveradresse aus der Settings XML holen
    public String getURL() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String val = sp.getString("server_address", "http://localhost:80");
        return val;
    }
}
