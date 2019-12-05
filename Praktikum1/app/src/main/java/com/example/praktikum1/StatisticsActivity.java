package com.example.praktikum1;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.praktikum1.graph.CdfGraphController;
import com.example.praktikum1.statistics.DataList;
import com.example.praktikum1.statistics.DoubleToString;
import com.example.praktikum1.statistics.TraitTable;
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
import java.util.Comparator;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticsActivity extends AppCompatActivity {

    private static String TAG = "STAT";

    // GUI-ELEMENTS
    private LinearLayout rootLayout;
    private Spinner routeSelector;
    private TextView statusTextView;
    // GraphView
    private GraphView cdfGraph;
    private CdfGraphController graphController;

    private final CSVParser CSV_PARSER = new CSVParserBuilder().withIgnoreQuotations(true).withSeparator(';').build();

    // Die Route, welche dargestellt wird
    private String route = "route1";

    // DATA
    private List<Location> flpHighLocationList;
    private List<Location> flpLowLocationList;
    private List<Location> lmLocationList;
    private List<Location> flagList;
    private List<Date> flpHighFlagTimestampList;
    private List<Date> flpLowFlagTimestampList;
    private List<Date> lmFlagTimestampList;

    private DataList flpHighErrorList;
    private DataList flpLowErrorList;
    private DataList lmErrorList;

    private TraitTable ttMedian, ttPercentil95, ttMean, ttStdError;

    private List<Location> interpolate(Location locationA, Location locationB, Date t1, Date t2) {
        List<Location> interpolated = new ArrayList<>();

        double dLat = locationB.getLatitude() - locationA.getLatitude();
        double dLong = locationB.getLongitude() - locationA.getLongitude();
        long step = Prak2.INTERVAL;
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

    private void clearData() {
        flpHighLocationList.clear();
        flpHighFlagTimestampList.clear();
        flpLowLocationList.clear();
        flpLowFlagTimestampList.clear();
        lmLocationList.clear();
        lmFlagTimestampList.clear();

        flpHighErrorList.clear();
        flpLowErrorList.clear();
        lmErrorList.clear();
    }

    private void readData() throws Exception {
        // Vor dem Lesen alle Datenlisten leeren, damit es nicht zu falschen
        // Berechnungen kommt, wenn bei einer Route z.B. Daten fehlen
        clearData();
        // LESEN
        File outputDir = new File(Constants.OUTPUT_DIR);
        String selectedRoute = route;

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

    /**
     * Teilweise werden Locations doppelt geloggt. Liegt wohl daran,
     * dass das Betriebssystem entscheidet, wann es welche Positionierung gibt.
     * Daher weren einfach die Locations, die denselben Timestamp haben rausgeworfen,
     * da es lediglich alle 3 Sekunden updates geben sollte.
     */
    private void cleanLocationLists() {
        cleanLocationList(flpHighLocationList);
        cleanLocationList(flpLowLocationList);
        cleanLocationList(lmLocationList);
    }

    private void cleanLocationList(List<Location> list) {
        List<Location> cleanedList = new ArrayList<>();
        Location lastLoc = list.get(0);
        for(int i = 1; i < list.size(); i++) {
            if(lastLoc.getTime() != list.get(i).getTime()) {
                cleanedList.add(list.get(i));
            }
            lastLoc = list.get(i);
        }

        // Zu guter letzt überschreiben
        list = cleanedList;
    }

    private DataList doStatistics(String type, List<Date> flagTimestampList, List<Location> locationList) {
        // Bevor die Rechnung gestartet werden kann
        if(flagTimestampList == null || locationList == null) return new DataList();
        if(flagTimestampList.size() < flagList.size()) return new DataList();

        // Und Daten bereinigen
        cleanLocationLists();

        // offset wird benötigt, da in der gesamten Timestamp-Liste die bereits
        // abgearbeiteten Timestamps berücksichtigt werden müssen
        DataList errorFlpHigh = new DataList();
        int offset = 0;
        for(int j = 1; j < flagList.size(); j++) {
            List<Location> interpolated = interpolate(
                    flagList.get(j - 1),
                    flagList.get(j),
                    flagTimestampList.get(j - 1),
                    flagTimestampList.get(j));

            Log.d(TAG, "Interpolated: " + interpolated.size());

            for(int i = 0; i < interpolated.size(); i++) {
                if(i + offset < locationList.size()) {
                    errorFlpHigh.add(interpolated.get(i).distanceTo(locationList.get(i + offset)));
                }
            }

            offset += interpolated.size() - 1;
        }

        Log.d(TAG, "Interpolated: " + locationList.size());

        Collections.sort(errorFlpHigh);

        // DataPoint(0,0) für Optik
        graphController.appendData(type, new double[] {0.0, 0.0});
        // Datenpunkte einhängen
        for(int i = 0; i < errorFlpHigh.size(); i++) {
            //Log.d(TAG, "X = " + errorFlpHigh.get(i) + " / Y = " + (double)(i+1) / errorFlpHigh.size());
            graphController.appendData(type, new double[] {errorFlpHigh.get(i), (double)(i+1) / errorFlpHigh.size()});
        }

        return errorFlpHigh;
    }

    private void loadDataAndDoStatistics() {
        graphController = new CdfGraphController(cdfGraph);

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

        if(doStatistics) {
            statusTextView.setText("");

            flpHighErrorList = doStatistics(Utils.TYPE_FLP_HIGH, flpHighFlagTimestampList, flpHighLocationList);
            flpLowErrorList = doStatistics(Utils.TYPE_FLP_LOW, flpLowFlagTimestampList, flpLowLocationList);
            lmErrorList = doStatistics(Utils.TYPE_LM_GPS, lmFlagTimestampList, lmLocationList);
        }
    }

    private void updateUI() {
        BiFunction<Double, Integer, Double> round = Utils::round;
        int digits = 3;
        // Gibt den Double-Wert als String zurück, oder den leeren String (bei d == -1.0)
        DoubleToString double2str = d -> d == -1.0 ? "" : "" + d;

        ttMedian.setContent(new String[] {
            double2str.convert(round.apply(flpHighErrorList.median(), digits)),
            double2str.convert(round.apply(flpLowErrorList.median(), digits)),
            double2str.convert(round.apply(lmErrorList.median(), digits))
        });

        ttPercentil95.setContent(new String[] {
            double2str.convert(round.apply(flpHighErrorList.percentile(95), digits)),
            double2str.convert(round.apply(flpLowErrorList.percentile(95), digits)),
            double2str.convert(round.apply(lmErrorList.percentile(95), digits))
        });

        ttMean.setContent(new String[] {
            double2str.convert(round.apply(flpHighErrorList.mean(), digits)),
            double2str.convert(round.apply(flpLowErrorList.mean(), digits)),
            double2str.convert(round.apply(lmErrorList.mean(), digits))
        });

        ttStdError.setContent(new String[] {
            double2str.convert(round.apply(flpHighErrorList.stdError(), digits)),
            double2str.convert(round.apply(flpLowErrorList.stdError(), digits)),
            double2str.convert(round.apply(lmErrorList.stdError(), digits))
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        flpHighLocationList = new ArrayList<>();
        flpHighFlagTimestampList = new ArrayList<>();
        flpLowLocationList = new ArrayList<>();
        flpLowFlagTimestampList = new ArrayList<>();
        lmLocationList = new ArrayList<>();
        lmFlagTimestampList = new ArrayList<>();

        flpHighErrorList = new DataList();
        flpLowErrorList = new DataList();
        lmErrorList = new DataList();

        rootLayout = findViewById(R.id.rootLayout);
        cdfGraph = findViewById(R.id.cdfGraph);
        statusTextView = findViewById(R.id.statusTextView);

        // Tabellen für die einzelnen Eigenschaften erstellen
        ttMedian = new TraitTable(getApplicationContext(), rootLayout, "Median");
        ttPercentil95 = new TraitTable(getApplicationContext(), rootLayout, "Fehler Konfidenz-Level 95%");
        ttMean = new TraitTable(getApplicationContext(), rootLayout, "Arithm. Mittel");
        ttStdError = new TraitTable(getApplicationContext(), rootLayout, "Standardabweichung");

        routeSelector = findViewById(R.id.routeSelector);
        routeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                route = parentView.getItemAtPosition(position).toString();
                loadDataAndDoStatistics();
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Kommt nicht vor
            }
        });

        //loadDataAndDoStatistics();


        // Weitere Statistische Kenngrößen
        // TODO: in eine Dataset-Klasse auslagern ?

        // Range
//        double flpHighRange = flpHighErrorList.stream().max(Comparator.naturalOrder()).get()
//                - flpHighErrorList.stream().min(Comparator.naturalOrder()).get();
//
//        flpHighRange = Math.round(flpHighRange * 1000) / 1000.0;
//
//        TraitTable tt = new TraitTable(getApplicationContext(), rootLayout, "Spannweite / range");
//        tt.setContent(new String[] {
//                flpHighRange + "m", "", ""
//        });
//
//        // Aritmethisches Mittel
//        double flpMean = 0;
//        for(float error : flpHighErrorList) {
//            flpMean += error;
//        }
//        flpMean /= flpHighErrorList.size();
//        flpMean = Math.round(flpMean * 1000) / 1000.0;
//
//        TraitTable ttMean = new TraitTable(getApplicationContext(), rootLayout, "Arithm. Mittel / mean");
//        ttMean.setContent(new String[] {
//                flpMean + "m", "", ""
//        });
//
//        // median
//        double flpMedian = 0;
//        if(flpHighErrorList.size() % 2 != 0) {
//            flpMedian = flpHighErrorList.get(flpHighErrorList.size() / 2 + 1);
//        }
//        else {
//            flpMedian = (flpHighErrorList.get(flpHighErrorList.size() / 2)
//                    + flpHighErrorList.get(flpHighErrorList.size() / 2 + 1)) / 2.0;
//        }
//
//        flpMedian = Math.round(flpMedian * 1000) / 1000.0;
//
//        TraitTable ttMedian = new TraitTable(getApplicationContext(), rootLayout, "Median");
//        ttMedian.setContent(new String[] {
//                flpMedian + "m", "", ""
//        });
    }
}
