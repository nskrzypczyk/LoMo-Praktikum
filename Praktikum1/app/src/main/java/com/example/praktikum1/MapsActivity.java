package com.example.praktikum1;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.opencsv.CSVReader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.IOException;
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

    public void getDataFromGET(){
        String URL = MainActivity.getInstance().getURL()+"/api/position/all";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Request req = new Request.Builder().url(URL).get().build();
                    Response res = MainActivity.getInstance().getClient().newCall(req).execute();
                    data = new JSONArray(res.body().string().toString());

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void  getDataFromCSV(){
        try {
            CSVReader reader = new CSVReader(new FileReader(Constants.GPS_OUTPUT_FILE_PATH));
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                //System.out.println(nextLine[0] + nextLine[1] + "etc...");
                String[] fields = nextLine[0].split(";");
                for(int i=0;i<fields.length;i++) {

                    fields[i]=fields[i].replaceAll("\"","");
                    fields[i]=fields[i].replaceAll(",",".");
                    System.out.println(fields[i]);
                }
                for(int i=0;i<fields.length;i++) {
                    try {
                        String longitude = fields[2];
                        String latitude = fields[1];
                        //System.out.println(latitude);
                        double longi = Double.parseDouble(longitude);
                        double lat = Double.parseDouble(latitude);
                        String stamp = fields[0];
                        LatLng pos = new LatLng(lat, longi);
                        mMap.addMarker(new MarkerOptions().position(pos).title(stamp));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
            reader.close();
        } catch (IOException e) {

        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            this.getDataFromGET();
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
            getDataFromCSV();
        }


        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

         */


    }
}
