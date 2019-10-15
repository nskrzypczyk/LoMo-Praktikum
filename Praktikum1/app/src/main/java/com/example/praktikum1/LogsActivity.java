package com.example.praktikum1;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        tableLayout = findViewById(R.id.tableLayout);

        showLogs(readLogs());
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
