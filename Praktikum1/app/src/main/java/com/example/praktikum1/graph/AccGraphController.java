package com.example.praktikum1.graph;

import android.graphics.Color;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class AccGraphController {

    private GraphView graph;
    private LineGraphSeries<DataPoint> xSeries, ySeries, zSeries;
    private double appendXValue = -1;

    public AccGraphController(GraphView graph) {
        this.graph = graph;

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(100);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-20);
        graph.getViewport().setMaxY(20);

        xSeries = new LineGraphSeries<>();
        xSeries.setTitle("X");
        xSeries.setColor(Color.BLUE);
        graph.addSeries(xSeries);

        ySeries = new LineGraphSeries<>();
        ySeries.setTitle("Y");
        ySeries.setColor(Color.GREEN);
        graph.addSeries(ySeries);

        zSeries = new LineGraphSeries<>();
        zSeries.setTitle("Z");
        zSeries.setColor(Color.RED);
        graph.addSeries(zSeries);
    }

    public void appendToGraph(float[] data) {
        appendXValue++;
        xSeries.appendData(new DataPoint(appendXValue, data[0]), true, 100);
        ySeries.appendData(new DataPoint(appendXValue, data[1]), true, 100);
        zSeries.appendData(new DataPoint(appendXValue, data[2]), true, 100);
    }
}
