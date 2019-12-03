package com.example.praktikum1;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.stream.IntStream;

import lombok.var;

public class MapsActivityP2 extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "p2MAPS";
    private GoogleMap mMap;
    static LocationBundle[] data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_p2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        data = getData();
        mMap = googleMap;
        var route1 = data[0];
        var route2 = data[1];
        this.drawLinesForEach(route1);
        this.drawLinesForEach(route2);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.4475,7.271034),15));
    }

    private void drawLinesForEach(LocationBundle bundle){
        try {
            IntStream.range(1, bundle.getFlagList().size()).forEach(n -> {
                var flag = bundle.getFlagList().get(n);
                var prevFlag = bundle.getFlagList().get(n - 1);
                var p2 = Utils.makeLatLong(flag);
                var p1 = Utils.makeLatLong(prevFlag);
                draw(p1, p2, Color.BLACK);
            });
        }catch(Exception e){
            Log.e(TAG,e.getMessage());
        }
        try{
            IntStream.range(1,bundle.getFlpHighLocationList().size()).forEach(n->{
                var flag = bundle.getFlpHighLocationList().get(n);
                var prevFlag  =bundle.getFlpHighLocationList().get(n-1);
                var p2 = Utils.makeLatLong(flag);
                var p1 = Utils.makeLatLong(prevFlag);
                draw(p1,p2, Color.RED);
            });
        }catch(Exception e){
            Log.e(TAG,e.getMessage());
        }

        try{
            IntStream.range(1,bundle.getFlpLowLocationList().size()).forEach(n->{
                var flag = bundle.getFlpLowLocationList().get(n);
                var prevFlag  =bundle.getFlpLowLocationList().get(n-1);
                var p2 = Utils.makeLatLong(flag);
                var p1 = Utils.makeLatLong(prevFlag);
                draw(p1,p2, Color.BLUE);
            });
        }catch(Exception e){
            Log.e(TAG,e.getMessage());
        }

        try {
            IntStream.range(1, bundle.getLmLocationList().size()).forEach(n -> {
                var flag = bundle.getLmLocationList().get(n);
                var prevFlag = bundle.getLmLocationList().get(n - 1);
                var p2 = Utils.makeLatLong(flag);
                var p1 = Utils.makeLatLong(prevFlag);
                draw(p1, p2, Color.GREEN);
            });
        }catch(Exception e){
            Log.e(TAG,e.getMessage());
        }
    }

    private void draw(LatLng p1, LatLng p2, int color){
        mMap.addPolyline(new PolylineOptions().add(p1).add(p2).color(color).width(4));
    }

    private static LocationBundle[] getData(){
        try {
            return new LocationBundle[]{Utils.getLocationBundleFromAllFiles("route1"), Utils.getLocationBundleFromAllFiles("route2")};
        }catch(Exception e){
            Log.e(TAG,"Beim Laden ist ein Fehler aufgetreten");
        }
        return new LocationBundle[0];
    }
}
