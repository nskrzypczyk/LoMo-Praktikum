package com.example.praktikum1;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity{
    Button btnUpdate, btnStop;
    TextView tvGPSLong, tvGPSLat, tvGPSAlt;
    LocationManager locManager;
    LocationListener locListener;
    SensorManager sensorManager;
    Location locGPS;
    OkHttpClient client;

    //Serveradresse aus XML holen
    public String getURL(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String val = sp.getString("server_address", "http://localhost:80");
        return val;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = new OkHttpClient();                                                //http client instanz
        setContentView(R.layout.activity_main);
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  //locationmanager instanz
        btnUpdate = findViewById(R.id.btnUpdate);
        btnStop = findViewById(R.id.btnStop);
        tvGPSLong = (TextView) findViewById(R.id.tvGPSLong);
        tvGPSLat = (TextView) findViewById(R.id.tvGPSLat);
        tvGPSAlt = findViewById(R.id.tvGPSAlt);
        //berechtigung für schreibzugriff auf externen speichr(SD)
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
                Position pos =new Position();
                try {
                    tvGPSLat.setText(location.getLatitude() + "");
                    tvGPSLong.setText(location.getLongitude() + "");
                    tvGPSAlt.setText(location.getAltitude()+"");
                    Date date =new Date(location.getTime());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY-HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("CET"));
                    String formattedDate= sdf.format(date);
                    Log.d("time", "onLocationChanged: "+formattedDate);
                    //Positionsobjekt erstellen
                    pos = new Position(formattedDate,location.getLatitude(), location.getLongitude(), location.getAltitude());


                } catch (Exception e) {
                    Log.e("OOF", "onLocationChanged: ", e.getCause());
                    tvGPSLat.setText("ERROR");
                    tvGPSLong.setText("ERROR");

                }
                try{
                    printToCSV(pos);
                }catch(Exception e){
                    e.printStackTrace();
                }
                try{
                    POSTrequest(pos);
                }catch(Exception e){
                    e.printStackTrace();
                    new AlertDialog.Builder(MainActivity.this).setTitle("Ein Fehler ist aufgetreten").setMessage("Der angegebene Server konnte nicht erreicht werden :(")
                    .setNeutralButton("Serverangabe prüfen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        }
                    }).create().show(); //TODO: Wird noch nicht angezeigt
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
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.actionmenu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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


    public void printToCSV(Position pos) throws IOException {
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
                fw.write(pos.getTimeStamp()+";"+pos.getLatitude()+";"+pos.getLongitude()+";"+pos.getAltitude()+"\n");

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

    public void POSTrequest(Position pos){
        System.out.println("URL: "+this.getURL());
        String URL = this.getURL();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MediaType JSON = MediaType.parse("application/json;charset=utf-8");
                    JSONObject actual = new JSONObject();
                    RequestBody body=null;
                    try {
                        actual.put("timeStamp", pos.getTimeStamp());
                        actual.put("lat", pos.getLatitude());
                        actual.put("long", pos.getLongitude());
                        actual.put("alt", pos.getAltitude());
                    }catch(Exception e){ e.printStackTrace();}
                    try{
                        body= RequestBody.create(JSON, actual.toString());
                        Request req = new Request.Builder().url(URL+"/api/position/send").post(body).build();
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
                btnStop.setVisibility(View.VISIBLE);
                btnUpdate.setVisibility(View.GONE);
        });
        btnStop.setOnClickListener(e->{
            locManager.removeUpdates(locListener);
            btnStop.setVisibility(View.GONE);
            btnUpdate.setVisibility(View.VISIBLE);
        });
    }


}
