package com.example.praktikum1;

import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import org.apache.commons.collections.bag.SynchronizedSortedBag;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import lombok.Getter;
import okhttp3.OkHttpClient;

public class BackgroundService extends Service {
    //TODO: Auslagern
    LocationListener locListener;
    SensorManager sensorManager;
    LocationManager locManager;
    Position position;

    String notificationText= "TEST";
    // Service-Gedöns
    Notification notification;
    public static final String CHANNEL_ID = "CID";

    @Override
    public void onCreate() {
        super.onCreate();

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // locationmanager instanz

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        locListener = new LocationListener() {
            @Override // wird aufgerufen, wenn es Positionsupdates gibt
            public void onLocationChanged(Location location) {
                if (location == null) {
                    System.out.println("Keine Daten");
                }
                Position pos = new Position();
                try {
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
                }
                try {
                    System.out.println("-----SERVICE: Eintrag wird zur CSV hinzugefuegt!-----");
                    MainActivity.getInstance().printToCSV();
                } catch (Exception e) {
                    e.printStackTrace();
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
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locManager.requestLocationUpdates(MainActivity.getInstance().getLocationProvider(), 3000, 0, locListener);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        this.notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("LOKALISATOREN DATENSAMMLER")
                .setContentText(this.notificationText)
                .setSmallIcon(R.drawable.bo_logo)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locManager.removeUpdates(locListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
