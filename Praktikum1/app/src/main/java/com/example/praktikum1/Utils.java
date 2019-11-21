package com.example.praktikum1;

import android.location.Location;
import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import org.apache.commons.collections.bag.SynchronizedSortedBag;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
    static final String TAG = "UTILS";
    public static final String TYPE_FLP_HIGH="FLP_HIGH";
    public static final String TYPE_FLP_LOW="FLP_LOW";
    public static final String TYPE_LM_GPS="LM_GPS";


    public static String getTimeStamp(Location location){
        Date date = new Date(location.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY-HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    public static String getTimeStampNow(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY-HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    /**
     * Helferlein, für das Schreiben von Log Datein für Prak2
     * @param provider: TYPE_FLP_HIGH / ...LOW oder TYPE_LM_GPS
     */
    public static File Prak2LogFile(String provider, String route){
        Log.i(TAG, "Versuche eine neue Datei anzulegen");
            String timeStamp = getTimeStampNow();
            final String OUTPUT_DIR = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator
                    + "LoMoPraktikum";
            final String OUTPUT_FILE_PATH = OUTPUT_DIR + File.separator + timeStamp + "_" + provider + "_" + route + ".csv";

            // ggf. Ausgabeverzeichnis erstellen, damit
            // im Anschluss problemlos geschrieben werden kann.
            File folder = new File(Constants.OUTPUT_DIR);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File fl = new File(OUTPUT_FILE_PATH);
            try {
                ICSVWriter writer = new CSVWriterBuilder(new FileWriter(fl))
                        .withSeparator(';').build();
                writer.writeNext(new String[]{"timeStamp", "latitude", "longitude", "altitude"});
                writer.close();
            }catch(Exception e){
                e.printStackTrace();
            }
            Log.i(TAG, fl.getAbsolutePath());
            return fl;
    }

    public void print(File file){


        try {
            // Sollte die Datei nicht existieren
            // -> Kopf der Tabelle schreiben
            if (!file.exists()) {
                ICSVWriter writer = new CSVWriterBuilder(new FileWriter(Constants.GPS_OUTPUT_FILE_PATH))
                        .withSeparator(';').build();
                writer.writeNext(new String[] { "timeStamp", "latitude", "longitude", "altitude" });
                writer.close();
            }

            // Daten des Positionsobjekts schreiben
            ICSVWriter writer = new CSVWriterBuilder(new FileWriter(Constants.GPS_OUTPUT_FILE_PATH, true))
                    .withSeparator(';').build();
            //writer.writeNext(position.toStringArray());
            writer.close();

        } catch (Exception e) {
        }
    }
}
