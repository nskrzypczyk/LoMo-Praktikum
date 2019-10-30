package com.example.praktikum1;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
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

import com.example.praktikum1.graph.AccGraphController;
import com.jjoe64.graphview.GraphView;
import com.opencsv.CSVParser;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    Button btnUpdate, btnStop;
    TextView tvGPSLong, tvGPSLat, tvGPSAlt, tvAcc,tvProx, tvAxis;
    LocationListener locListener;
    SensorManager sensorManager;

    // Implementierte Sensoren/Services
    LocationManager locManager; // --> NUR GPS (siehe configureButton()
    Sensor accelerometer;       // --> Beschleunigung (misst die kräfte, die auf den sensor wirken[siehe android reference seite]
    Sensor proximity;           // --> in meinem Fall nur binaer, also kleinste/groesste entfernung
    Sensor orientation;         // --> Werte in °, positiv in mathematisch negativer richtung...oder so


    Location locGPS;
    @Getter
    OkHttpClient client;
    Position position;
    Sensordaten sensordaten = new Sensordaten();
    @Getter
    public static MainActivity instance;
    // GraphView
    GraphView accGraph;
    AccGraphController accGraphController;

    // Serveradresse aus der Settings XML holen
    public String getURL() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String val = sp.getString("server_address", "http://localhost:80");
        return val;
    }

    //Sensor Delay aus der Settings XML holen
    public int getSensorDelay(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String val = sp.getString("sensor_delay", "SENSOR_DELAY_NORMAL");
        switch(val){
            case "SENSOR_DELAY_NORMAL":
                return SensorManager.SENSOR_DELAY_NORMAL;
            case "SENSOR_DELAY_UI":
                return SensorManager.SENSOR_DELAY_UI;
            case "SENSOR_DELAY_GAME":
                return SensorManager.SENSOR_DELAY_GAME;
            case "SENSOR_DELAY_FASTEST":
                return SensorManager.SENSOR_DELAY_FASTEST;
            default:
                return SensorManager.SENSOR_DELAY_NORMAL;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance=this;
        client = new OkHttpClient(); // http client instanz
        setContentView(R.layout.activity_main);
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // locationmanager instanz
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnStop = findViewById(R.id.btnStop);
        tvGPSLong = (TextView) findViewById(R.id.tvGPSLong);
        tvGPSLat = (TextView) findViewById(R.id.tvGPSLat);
        tvGPSAlt = findViewById(R.id.tvGPSAlt);
        tvAcc = findViewById(R.id.tvAcc);
        tvProx = findViewById(R.id.tvProx);
        tvAxis = findViewById(R.id.tvAxis);

        accGraph = findViewById(R.id.accGraph);
        accGraphController = new AccGraphController(accGraph);

        // berechtigung für schreibzugriff auf externen speichr(SD)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE }, 10);
            }
        }

        locListener = new LocationListener() {
            @Override // wird aufgerufen, wenn es Positionsupdates gibt
            public void onLocationChanged(Location location) {
                if (location == null) {
                    System.out.println("Keine Daten");
                }
                Position pos = new Position();
                try {
                    tvGPSLat.setText(location.getLatitude() + "");
                    tvGPSLong.setText(location.getLongitude() + "");
                    tvGPSAlt.setText(location.getAltitude() + "");
                    Date date = new Date(location.getTime());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY-HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("CET"));
                    String formattedDate = sdf.format(date);
                    Log.d("time", "onLocationChanged: " + formattedDate);
                    // Positionsobjekt erstellen
                    position = new Position(formattedDate, location.getLatitude(), location.getLongitude(),
                            location.getAltitude());

                } catch (Exception e) {
                    Log.e("OOF", "onLocationChanged: ", e.getCause());
                    tvGPSLat.setText("ERROR");
                    tvGPSLong.setText("ERROR");

                }
                try {
                    printToCSV();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    POSTrequest();
                } catch (Exception e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(MainActivity.this).setTitle("Ein Fehler ist aufgetreten")
                            .setMessage("Der angegebene Server konnte nicht erreicht werden :(")
                            .setNeutralButton("Serverangabe prüfen", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
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

            @Override // checkt ob Standort aus ist
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        configureButton();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.actionmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.action_settings:
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.actionMenu_logs:
                intent = new Intent(MainActivity.this, LogsActivity.class);
                startActivity(intent);
                return true;
            case R.id.actionMenu_graphics:
                intent = new Intent(MainActivity.this, MapsActivity.class);
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
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public void printToCSV() throws IOException {
        final String OUTPUT_DIR = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator
                + "LoMoPraktikum";
        final String OUTPUT_FILE_PATH = OUTPUT_DIR + File.separator + "gps-daten.csv";

        // ggf. Ausgabeverzeichnis erstellen, damit
        // im Anschluss problemlos geschrieben werden kann.
        File folder = new File(Constants.OUTPUT_DIR);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(Constants.GPS_OUTPUT_FILE_PATH);

        try {
            // Sollte die Datei nicht existieren
            // -> Kopf der Tabelle schreiben
            if (!file.exists()) {
                ICSVWriter writer = new CSVWriterBuilder(new FileWriter(Constants.GPS_OUTPUT_FILE_PATH))
                        .withSeparator(';').build();
                writer.writeNext(new String[] { "timeStamp", "latitude", "longitude", "altitude" });
                writer.close();
            }

            // Daten des Positionsobjekts schreiben
            ICSVWriter writer = new CSVWriterBuilder(new FileWriter(Constants.GPS_OUTPUT_FILE_PATH, true))
                    .withSeparator(';').build();
            writer.writeNext(position.toStringArray());
            writer.close();

        } catch (Exception e) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        switch (requestCode) {
        case 10:
            configureButton();
            break;
        default:
            break;
        }
    }

    public void POSTrequest() {
        System.out.println("URL: " + this.getURL());
        String URL = this.getURL();
        new Thread(new Runnable() {
            @Override
            public void run() {
                MediaType JSON = MediaType.parse("application/json;charset=utf-8");
                JSONObject actual = new JSONObject();
                RequestBody body = null;
                try {
                    actual.put("timeStamp", position.getTimeStamp());
                    actual.put("lat", position.getLatitude());
                    actual.put("long", position.getLongitude());
                    actual.put("alt", position.getAltitude());
                    actual.put("accX", sensordaten.getAccX());
                    actual.put("accY", sensordaten.getAccY());
                    actual.put("accZ", sensordaten.getAccZ());
                    actual.put("prox",sensordaten.getProx());
                    actual.put("axisX", sensordaten.getAxisX());
                    actual.put("axisY", sensordaten.getAxisY());
                    actual.put("axisZ", sensordaten.getAxisZ());
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

    // WIRD VOM LOCATIONLISTENER AUFGERUFEN
    private void configureButton() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET }, 10);
            }
            return;
        }
        btnUpdate.setOnClickListener(e -> {
            locManager.requestLocationUpdates("gps", 3000, 0, locListener);
            btnStop.setVisibility(View.VISIBLE);
            btnUpdate.setVisibility(View.GONE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            onResume();
        });

        btnStop.setOnClickListener(e -> {
            locManager.removeUpdates(locListener);
            btnStop.setVisibility(View.GONE);
            btnUpdate.setVisibility(View.VISIBLE);

            onPause();
        });
    }

    // SENSORMANAGER / SENSOREVENTLISTENER METHODEN
    protected void onResume() {
        super.onResume();
        int delay = this.getSensorDelay();
        sensorManager.registerListener(this, accelerometer, delay);
        sensorManager.registerListener(this,proximity, delay);
        sensorManager.registerListener(this,orientation, delay);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
        case Sensor.TYPE_ACCELEROMETER:
            tvAcc.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
            this.sensordaten.setAccX(event.values[0]);
            this.sensordaten.setAccY(event.values[1]);
            this.sensordaten.setAccZ(event.values[2]);
            accGraphController.appendToGraph(event.values);
            break;
        case Sensor.TYPE_PROXIMITY:
            tvProx.setText(event.values[0] +"cm");
            this.sensordaten.setProx(event.values[0]);
            break;
        case Sensor.TYPE_ORIENTATION:
            tvAxis.setText("X: "+ event.values[1]+"\nY:" + event.values[2]+"\nZ: "+ event.values[0]);
            this.sensordaten.setAxisX(event.values[1]);
            this.sensordaten.setAxisY(event.values[2]);
            this.sensordaten.setAxisZ(event.values[0]);
            break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
