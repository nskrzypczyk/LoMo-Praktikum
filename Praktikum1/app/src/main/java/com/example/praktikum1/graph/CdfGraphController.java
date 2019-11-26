package com.example.praktikum1.graph;

import android.graphics.Color;

import com.example.praktikum1.Utils;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.stream.Stream;

public class CdfGraphController {
    private GraphView graph;
    private LineGraphSeries<DataPoint> flpHighSeries, flpLowSeries, lmSeries;

    public CdfGraphController(GraphView graph) {
        this.graph = graph;

        this.reset();

        graph.setTitle("CDF-Graph");

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);

        graph.getGridLabelRenderer().setPadding(40);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(1);

        flpHighSeries = new LineGraphSeries<>();
        flpHighSeries.setTitle(Utils.TYPE_FLP_HIGH);
        flpHighSeries.setColor(Color.BLUE);
        graph.addSeries(flpHighSeries);

        flpLowSeries = new LineGraphSeries<>();
        flpLowSeries.setTitle(Utils.TYPE_FLP_LOW);
        flpLowSeries.setColor(Color.GREEN);
        graph.addSeries(flpLowSeries);

        lmSeries = new LineGraphSeries<>();
        lmSeries.setTitle(Utils.TYPE_LM_GPS);
        lmSeries.setColor(Color.RED);
        graph.addSeries(lmSeries);
    }

    public void appendData(String series, double[] data) {
        switch(series) {
            case Utils.TYPE_FLP_HIGH:
                flpHighSeries.appendData(new DataPoint(data[0], data[1]), false, 99999);
                break;
            case Utils.TYPE_FLP_LOW:
                flpLowSeries.appendData(new DataPoint(data[0], data[1]), false, 99999);
                break;
            case Utils.TYPE_LM_GPS:
                lmSeries.appendData(new DataPoint(data[0], data[1]), false, 99999);
                break;
        }

        // Anzahl der Label auf der X-Achse + xmax aktualisieren
        int max = Stream.of(flpHighSeries.getHighestValueX(), flpLowSeries.getHighestValueX(), lmSeries.getHighestValueX())
                .mapToInt(n -> n.intValue())
                .max()
                .getAsInt();

        graph.getViewport().setMaxX(max + 2); // +2 für Padding

        if(max > 20) {
            max /= 10;
        }

        graph.getGridLabelRenderer().setNumHorizontalLabels(max);
    }

    private void reset() {
        graph.getSeries().clear();
    }
}
