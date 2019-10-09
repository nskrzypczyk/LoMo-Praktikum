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
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

                    printToCSV(new String[]{formattedDate,location.getLatitude()+"",location.getLongitude()+""});

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


    public void printToCSV(String[] data) throws IOException {
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
                fw.write(data[0]+";"+data[1]+";"+data[2]+"\n");

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
