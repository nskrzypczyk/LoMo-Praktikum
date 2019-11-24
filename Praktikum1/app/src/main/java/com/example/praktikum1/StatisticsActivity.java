package com.example.praktikum1;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticsActivity extends AppCompatActivity {

    private static String TAG = "STAT";

    // GUI-ELEMENTS
    private Spinner routeSelector;
    private TextView statusTextView;
    // GraphView
    private GraphView cdfGraph;

    private final CSVParser CSV_PARSER = new CSVParserBuilder().withIgnoreQuotations(true).withSeparator(';').build();

    // DATA
    private List<Location> flpHighLocationList;
    private List<Location> flpLowLocationList;
    private List<Location> lmLocationList;
    private List<Location> flagList;
    private List<Date> flpHighFlagTimestampList;
    private List<Date> flpLowFlagTimestampList;
    private List<Date> lmFlagTimestampList;

    private List<Location> interpolate(Location locationA, Location locationB, Date t1, Date t2) {
        List<Location> interpolated = new ArrayList<>();

        double dLat = locationB.getLatitude() - locationA.getLatitude();
        double dLong = locationB.getLongitude() - locationA.getLongitude();
        int step = Constants.INTERVAL;
        long t21 = t2.getTime() - t1.getTime();
        long t = t1.getTime() + step;

        while(t < t2.getTime()) {
            double dt = (t - t1.getTime()) / t21;
            Location loc = new Location("interpolatedLocation");
            loc.setLatitude(locationA.getLatitude() + dLat * dt);
            loc.setLongitude(locationA.getLongitude() + dLong * dt);
            interpolated.add(loc);

            t = t + step;
        }

        return interpolated;
    }

    private void readData() throws Exception {
        File outputDir = new File(Constants.OUTPUT_DIR);
        String selectedRoute = "route1";

        List<File> files = Arrays.stream(outputDir.list())
                .filter(name -> name.contains(selectedRoute))
                .map(str -> new File(outputDir + File.separator + str)).collect(Collectors.toList());

        files.forEach(file  -> Log.d(TAG, file.getName()));

        // Flags der Route auslesen
        File flagFile = files.stream().filter(f -> f.getName().equals(selectedRoute + ".csv")).findFirst().get();
        CSVReader flagReader = new CSVReaderBuilder(new FileReader(flagFile))
                .withCSVParser(CSV_PARSER)
                .build();

        List<String[]> entries = flagReader.readAll();
        flagList = entries.stream().map(entry -> {
            Location loc = new Location("flag");
            loc.setLatitude(Double.parseDouble(entry[0]));
            loc.setLongitude(Double.parseDouble(entry[1]));
            return loc;
        }).collect(Collectors.toList());

        // Dateien mit den Positionen lesen
        Optional<File> opt = files.stream().filter(f -> f.getName().contains(Utils.TYPE_FLP_HIGH)).findFirst();
        if(opt.isPresent() && opt.get().exists()) {
            flpHighLocationList = readRecordedLocations(opt.get());
        }

        opt = files.stream().filter(f -> f.getName().contains(Utils.TYPE_FLP_LOW)).findFirst();
        if(opt.isPresent() && opt.get().exists()) {
            flpLowLocationList = readRecordedLocations(opt.get());
        }

        opt = files.stream().filter(f -> f.getName().contains(Utils.TYPE_LM_GPS)).findFirst();
        if(opt.isPresent() && opt.get().exists()) {
            lmLocationList = readRecordedLocations(opt.get());
        }

        // Die Timestamps für die Flags holen
        String flpHighTimestampFilePattern = Utils.TYPE_FLP_HIGH + "_" + selectedRoute + ".timestamp.csv";
        opt = files.stream().filter(f -> f.getName().contains(flpHighTimestampFilePattern)).findFirst();
        if(opt.isPresent() && opt.get().exists()) {
            flpHighFlagTimestampList = readRecordedTimestamps(opt.get());
        }

        String flpLowTimestampFilePattern = Utils.TYPE_FLP_LOW + "_" + selectedRoute + ".timestamp.csv";
        opt = files.stream().filter(f -> f.getName().contains(flpLowTimestampFilePattern)).findFirst();
        if(opt.isPresent() && opt.get().exists()) {
            flpLowFlagTimestampList = readRecordedTimestamps(opt.get());
        }

        String lmTimestampFilePattern = Utils.TYPE_LM_GPS + "_" + selectedRoute + ".timestamp.csv";
        opt = files.stream().filter(f -> f.getName().contains(lmTimestampFilePattern)).findFirst();
        if(opt.isPresent() && opt.get().exists()) {
            lmFlagTimestampList = readRecordedTimestamps(opt.get());
        }
    }

    private List<Location> readRecordedLocations(File file) throws IOException {
        CSVReader reader = new CSVReaderBuilder(new FileReader(file))
                .withCSVParser(CSV_PARSER)
                .withSkipLines(1)
                .build();
        List<String[]> entries = reader.readAll();

        return entries.stream().map(e -> {
            Location loc = new Location("RecordedLocation");
            loc.setLatitude(Double.parseDouble(e[1]));
            loc.setLongitude(Double.parseDouble(e[2]));
            loc.setAltitude(Double.parseDouble(e[3]));
            try {
                loc.setTime(Utils.convertStringToDate(e[0]).getTime());
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            return loc;
        }).collect(Collectors.toList());
    }

    private List<Date> readRecordedTimestamps(File file) throws IOException {
        CSVReader reader = new CSVReaderBuilder(new FileReader(file))
                .withCSVParser(CSV_PARSER)
                .build();

        return reader.readAll().stream()
            .filter(line -> line.length >= 1)
            .map(line -> {
                try {
                    return Utils.convertStringToDate(line[0]);
                } catch (ParseException e) {}
                return null;
            })
            .filter(date -> date != null)
            .collect(Collectors.toList());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        routeSelector = findViewById(R.id.routeSelector);
        cdfGraph = findViewById(R.id.cdfGraph);
        statusTextView = findViewById(R.id.statusTextView);

        boolean doStatistics = false;
        try {
            readData();
            doStatistics = true;
        } catch (IOException ioe) {
            statusTextView.setText("ERROR: Eine IO-Exception ist aufgetreten!");
            Log.e(TAG, ioe.getMessage());
        } catch (NumberFormatException nfe) {
            statusTextView.setText("ERROR: Der Inhalt einer Datei ist inkompatibel!");
        } catch (Exception e) {
            statusTextView.setText("ERROR: " + e.getMessage());
        }

        // TODO: in GraphController auslagern
        cdfGraph.getViewport().setMinY(0);
        cdfGraph.getViewport().setMaxY(1);
        cdfGraph.getViewport().setYAxisBoundsManual(true);

        if(doStatistics) {
            statusTextView.setText("STATUS: Datenformat korrekt!");

            // TODO: Verallgemeinern und gegen Fehler absichern
            List<Location> interpolated = interpolate(
                    flagList.get(0),
                    flagList.get(1),
                    flpHighFlagTimestampList.get(0),
                    flpHighFlagTimestampList.get(1));

            List<Float> errorFlpHigh = new LinkedList<>();
            for(int i = 0; i < interpolated.size(); i++) {
                errorFlpHigh.add(interpolated.get(i).distanceTo(flpHighLocationList.get(i)));
            }

            Collections.sort(errorFlpHigh);

            LineGraphSeries<DataPoint> flpHighSeries = new LineGraphSeries<>();
            cdfGraph.addSeries(flpHighSeries);

            cdfGraph.getViewport().setXAxisBoundsManual(true);
            cdfGraph.getViewport().setMaxX(errorFlpHigh.get(errorFlpHigh.size()-1) + 2); // +2 für padding

            // DataPoint(0,0) für Optik
            flpHighSeries.appendData(new DataPoint(0, 0), false, 100);
            // Datenpunkte einhängen
            for(int i = 0; i < errorFlpHigh.size(); i++) {
                Log.d(TAG, "X = " + errorFlpHigh.get(i) + " / Y = " + (double)(i+1) / errorFlpHigh.size());
                flpHighSeries.appendData(new DataPoint(errorFlpHigh.get(i), (double)(i+1) / errorFlpHigh.size()), false, 100);
            }
        }
    }
}
