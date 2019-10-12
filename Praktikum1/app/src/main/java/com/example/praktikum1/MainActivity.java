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
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.textclassifier.TextLinks;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import lombok.var;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    Button btnUpdate;
    TextView tvGPSLong, tvGPSLat;
    LocationManager locManager;
    LocationListener locListener;
    Location locGPS;
    OkHttpClient client;
    static final String URL = "http://LOKALE IP HIER!!!:80";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = new OkHttpClient();
        setContentView(R.layout.activity_main);
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        btnUpdate = findViewById(R.id.btnUpdate);
        //btnUpdate.setOnClickListener(e -> this.getLocationData());
        tvGPSLong = (TextView) findViewById(R.id.tvGPSLong);
        tvGPSLat = (TextView) findViewById(R.id.tvGPSLat);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}
                        , 10);
            }
        }


        locListener = new LocationListener() {
            @Override //wird aufgerufen, wenn es Positionsupdates gibt
            public void onLocationChanged(Location location) {
                if(location==null){
                    System.out.println("S.H.I.T.");
                }
                try {
                    tvGPSLat.setText(location.getLatitude() + "");
                    tvGPSLong.setText(location.getLongitude() + "");
                    Date date =new Date(location.getTime());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY-HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("CET"));
                    String formattedDate= sdf.format(date);
                    Log.d("time", "onLocationChanged: "+formattedDate);
                    //Positionsobjekt erstellen
                    Position pos = new Position(formattedDate,location.getLatitude(), location.getLongitude());
                    printToCSV(pos.toHashMap());
                    POSTrequest(pos.toHashMap());

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

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }


    public void printToCSV(HashMap<String, String> hashMap) throws IOException {
        {

            File root = Environment.getExternalStorageDirectory();
            File folder = new File(root.getAbsoluteFile()+"/Ergebnisse");

            boolean var = false;
            if (!folder.exists())
                var = folder.mkdirs();

            System.out.println("" + var);


            File file = new File(folder,"ergebnis.csv");

            try {
                FileWriter fw = new FileWriter(file, true);
                fw.write(hashMap.get("timeStamp")+";"+hashMap.get("latitude")+";"+hashMap.get("longitude")+"\n");

                fw.flush();
                fw.close();

            } catch (Exception e) {
            }

        }

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

    public void POSTrequest(HashMap<String,String> hashMap){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MediaType JSON = MediaType.parse("application/json;charset=utf-8");
                    JSONObject actual = new JSONObject();
                    try {
                        actual.put("timeStamp", hashMap.get("timeStamp"));
                        actual.put("lat", hashMap.get("latitude"));
                        actual.put("long", hashMap.get("longitude"));
                    }catch(Exception e){ e.printStackTrace();}
                        RequestBody body = RequestBody.create(JSON, actual.toString());
                        Request req = new Request.Builder().url(MainActivity.URL+"/api/position/send").post(body).build();
                    try {
                        Response res = client.newCall(req).execute();
                        System.out.println(res.toString());
                    }catch(Exception e){e.printStackTrace();}
                        System.out.println(body);
                }
            }).start();

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
                locManager.requestLocationUpdates("gps", 3000, 0, locListener);
        });
    }

}
