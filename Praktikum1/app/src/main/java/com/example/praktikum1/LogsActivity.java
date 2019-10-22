package com.example.praktikum1;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogsActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private TextView distanceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        tableLayout = findViewById(R.id.tableLayout);
        distanceTextView = findViewById(R.id.distanceTextView);

        List<String[]> logs = readLogs();
        showLogs(logs);
        calculateDistance(logs);
    }

    private void showLogs(List<String[]> logs) {
        for(String[] log : logs) {
            TableRow tableRow = new TableRow(this);

            for(int i = 0; i < log.length; i++) {

                // ggf. den String trimmen, damit alles in die Tabelle passt
                if(i > 0) {
                    log[i] = log[i].substring(0,7);
                }

                TextView textView = new TextView(this);
                textView.setText(log[i]);
                tableRow.addView(textView);
            }

            tableLayout.addView(tableRow);
        }
    }

    private void calculateDistance(List<String[]> logs) {
        final int LAT_INDEX = 1;
        final int LONG_INDEX = 2;
        final int ALT_INDEX = 3;

        float distance = 0;
        for(int i = 1; i < logs.size(); i++) {
            String[] curLog = logs.get(i);
            String[] prevLog = logs.get(i - 1);

            Location curLocation = new Location("");
            curLocation.setLongitude(Double.parseDouble(curLog[LONG_INDEX].replace(',', '.')));
            curLocation.setLatitude(Double.parseDouble(curLog[LAT_INDEX].replace(',', '.')));
            curLocation.setAltitude(Double.parseDouble(curLog[ALT_INDEX].replace(',', '.')));

            Location prevLocation = new Location("");
            prevLocation.setLongitude(Double.parseDouble(prevLog[LONG_INDEX].replace(',', '.')));
            prevLocation.setLatitude(Double.parseDouble(prevLog[LAT_INDEX].replace(',', '.')));
            prevLocation.setAltitude(Double.parseDouble(prevLog[ALT_INDEX].replace(',', '.')));

            distance += curLocation.distanceTo(prevLocation);
        }

        distanceTextView.setText(distance + "m");
    }

    private List<String[]> readLogs() {
        CSVParser parser = new CSVParserBuilder().withIgnoreQuotations(true).withSeparator(';').build();

        List<String[]> logs = new ArrayList<>();
        try(
            CSVReader reader = new CSVReaderBuilder(new FileReader(Constants.GPS_OUTPUT_FILE_PATH))
                .withSkipLines(1)
                .withCSVParser(parser)
                .build()
        ) {
            logs = reader.readAll();
        }
        catch (IOException ioe) {
            // todo
        }

        return logs;
    }
}
