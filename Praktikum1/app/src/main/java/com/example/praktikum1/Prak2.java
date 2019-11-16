package com.example.praktikum1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class Prak2 extends AppCompatActivity {
    private
    private Spinner spRoutes, spProvider;
    private TextView tvGPSLat, tvGPSLong;
    private Button btnStart, btnTimestamp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prak2);

        this.initComponents();
        this.initListeners();
    }

    private void initComponents(){
        this.spRoutes=findViewById(R.id.spRoutes);
        this.spProvider=findViewById(R.id.spProvider);
        this.tvGPSLat = findViewById(R.id.tvLat);
        this.tvGPSLong=findViewById(R.id.tvLong);
        this.btnStart = findViewById(R.id.btnStart);
        this.btnTimestamp = findViewById(R.id.btnTimestamp);
    }

    private void initListeners(){
        spProvider.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
}
