package com.example.praktikum1;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private JSONArray data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void getData(){
        String URL = MainActivity.getInstance().getURL()+"/api/position/all";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Request req = new Request.Builder().url(URL).get().build();
                    Response res = MainActivity.getInstance().getClient().newCall(req).execute();
                    //System.out.println(res.body().toString());
                    data = new JSONArray(res.body().string().toString());

                }catch(Exception e){
                    e.printStackTrace();
                }

                /*try {
                    System.out.println("");
                    LatLng pos = new LatLng(lat,longi);
                    mMap.addMarker(new MarkerOptions().position(pos).title(stamp));

                    }
                catch(Exception e){
                    e.printStackTrace();
                }*/
            }
        }).start();
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            this.getData();
            Thread.sleep(5000);
            for(int i=0;i<data.length();i++){
                System.out.println(data.getJSONObject(i).get("long").toString());
                double longi = Double.parseDouble(data.getJSONObject(i).get("long").toString());
                double lat = Double.parseDouble(data.getJSONObject(i).get("lat").toString());
                String stamp = data.getJSONObject(i).get("timeStamp").toString();
                LatLng pos = new LatLng(lat,longi);
                mMap.addMarker(new MarkerOptions().position(pos).title(stamp));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

         */


    }
}
