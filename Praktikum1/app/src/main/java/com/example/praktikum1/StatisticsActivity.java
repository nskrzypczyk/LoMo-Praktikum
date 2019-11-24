package com.example.praktikum1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;

public class StatisticsActivity extends AppCompatActivity {

    // GUI-ELEMENTS
    private Spinner routeSelector;
    private TextView statusTextView;
    // GraphView
    private GraphView cdfGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        routeSelector = findViewById(R.id.routeSelector);
        cdfGraph = findViewById(R.id.cdfGraph);
        statusTextView = findViewById(R.id.statusTextView);
    }
}
