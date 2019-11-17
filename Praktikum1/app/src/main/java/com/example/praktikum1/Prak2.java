package com.example.praktikum1;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.LocationListener;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class Prak2 extends AppCompatActivity {
    private Spinner spRoutes, spProvider;
    private TextView tvLat, tvLong;
    private Button btnStart, btnTimestamp;
    FusedLocation fusedLocation;

    public static String chosenRoute;
    public static String chosenProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prak2);


        this.initComponents();
        this.initListeners();
        this.initAPIs();
    }

    private void initAPIs(){
        fusedLocation = new FusedLocation(this, new FusedLocation.Callback() {
            @Override
            public void onLocationResult(Location location) {
                //Do as you wish with location here
                tvLong.setText(location.getLongitude() + "");
                tvLat.setText(location.getLatitude()+"");

            }
        });
    }

    private void initComponents(){
        this.spRoutes=findViewById(R.id.spRoutes);
        this.spProvider=findViewById(R.id.spProvider);
        this.tvLat = findViewById(R.id.tvLat);
        this.tvLong =findViewById(R.id.tvLong);
        this.btnStart = findViewById(R.id.btnStart);
        this.btnTimestamp = findViewById(R.id.btnTimestamp);

        chosenProvider = spProvider.getSelectedItem().toString();
    }

    private void initListeners() {
        spProvider.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        btnStart.setOnClickListener(e-> {
            fusedLocation.startLocationUpdates();
        });
    }

}
