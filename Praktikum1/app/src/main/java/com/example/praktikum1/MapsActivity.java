package com.example.praktikum1;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.opencsv.CSVReader;

import org.apache.commons.collections.ArrayStack;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import lombok.var;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private JSONArray data;
    private List<LatLng> posList = new ArrayList<>();
    private HeatmapTileProvider mProvider=null;
    private TileOverlay mOverlay=null;
    private List<Marker> markerList = new ArrayList<>();
    private Button buttonHeatmap;
    private Button buttonMarkers;
    private TextView lblMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buttonHeatmap =findViewById(R.id.buttonHeatmap);
        buttonHeatmap.setOnClickListener(e->{
            if(mOverlay.isVisible()){
                mOverlay.setVisible(false);
            }else{
                mOverlay.setVisible(true);
            }
        });
        buttonMarkers=findViewById(R.id.buttonMarkers);
        buttonMarkers.setOnClickListener(e->{
            if(this.markerList.get(0).isVisible()){
                for(int i=0;i<markerList.size();i++){
                    markerList.get(i).setVisible(false);
                }
            }else{
                for(int i=0;i<markerList.size();i++){
                    markerList.get(i).setVisible(true);
                }
            }
        });
        lblMode = findViewById(R.id.lblMode);

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
                for (int i = 0; i < fields.length; i++) {

                    fields[i] = fields[i].replaceAll("\"", "");
                    fields[i] = fields[i].replaceAll(",", ".");
                    System.out.println(fields[i]);
                }
                for (int i = 0; i < fields.length; i++) {
                    try {
                        String longitude = fields[2];
                        String latitude = fields[1];
                        //System.out.println(latitude);
                        double longi = Double.parseDouble(longitude);
                        double lat = Double.parseDouble(latitude);
                        String stamp = fields[0];
                        LatLng pos = new LatLng(lat, longi);
                        this.posList.add(pos);
                        Marker m = mMap.addMarker(new MarkerOptions().position(pos).title(stamp));
                        this.markerList.add(m);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
            lblMode.setText("DATEN AUS CSV!");
            reader.close();
        } catch (IOException e) {

        }
    }
    public void addHeatMap(){
        for(int i=0;i<this.posList.size();i++){
            System.out.println(posList.get(i).latitude);
        }
        mProvider = new HeatmapTileProvider.Builder().data(this.posList).build();
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            this.getDataFromGET();
            Thread.sleep(3000);
            for(int i=0;i<data.length();i++){
                System.out.println(data.getJSONObject(i).get("long").toString());
                double longi = Double.parseDouble(data.getJSONObject(i).get("long").toString());
                double lat = Double.parseDouble(data.getJSONObject(i).get("lat").toString());
                String stamp = data.getJSONObject(i).get("timeStamp").toString();
                LatLng pos = new LatLng(lat,longi);
                this.posList.add(pos);
                Marker m = mMap.addMarker(new MarkerOptions().position(pos).title(stamp));
                this.markerList.add(m);

            }
            lblMode.setText("DATEN VOM SERVER!");
        } catch (Exception e) {
            e.printStackTrace();
            getDataFromCSV();
        }finally{
            if(posList.size()!=0){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posList.get(0),15));
            }
            this.addHeatMap();
            mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }


        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

         */


    }
}
