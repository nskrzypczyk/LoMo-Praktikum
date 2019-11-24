package com.example.praktikum1;

import android.location.Location;
import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import org.apache.commons.collections.bag.SynchronizedSortedBag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
    static final String TAG = "UTILS";
    public static final String TYPE_FLP_HIGH="FLP_HIGH";
    public static final String TYPE_FLP_LOW="FLP_LOW";
    public static final String TYPE_LM_GPS="LM_GPS";
    private static String timeStamp;

    public static final String TIMESTAMP_PATTERN = "dd.MM.YYYY-HH:mm:ss";

    /**
     * Gibt den custom Timestamp String der übergebenen Location zurück
     * @param location
     * @return
     */
    public static String getTimeStamp(Location location){
        Date date = new Date(location.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY-HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    /**
     * Gibt die aktuelle Zeit als custom timestamp String zurück
     * @return
     */
    public static String getTimeStampNow(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY-HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    public static Date convertStringToDate(String dateString) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(TIMESTAMP_PATTERN);
        formatter.setTimeZone(TimeZone.getTimeZone("CET"));
        return formatter.parse(dateString);
    }

    public static void printToCSV(File file, String timeStamp, Location... loc){
        try {
            if (!file.exists()) {
                throw new FileNotFoundException("Datei nicht gefunden, oof");
            }
            ICSVWriter writer = new CSVWriterBuilder(new FileWriter(file, true))
                    .withSeparator(';').build();
            if(loc.length<=0){
                Log.i(TAG, "SCHREIBE IN DIE TIMESTAMP DATEI");
                writer.writeNext(new String[]{timeStamp});
            }
            else{
                Log.i(TAG, "SCHREIBE IN DIE LOGFILE DATEI");
                writer.writeNext(new String[]{timeStamp,loc[0].getLatitude()+"",loc[0].getLongitude()+"",loc[0].getAltitude()+""});
            }
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helferlein, für das Schreiben von Log Dateien für Prak2
     * @param provider: TYPE_FLP_HIGH / ...LOW oder TYPE_LM_GPS
     */
    public static File Prak2LogFile(String provider, String route, String... args){
        if(args.length==0) {
            Log.i(TAG, "Versuche eine neue Log Datei anzulegen");
            timeStamp = getTimeStampNow();
            final String OUTPUT_DIR = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator
                    + "LoMoPraktikum";
            final String OUTPUT_FILE_PATH = OUTPUT_DIR + File.separator + timeStamp + "_" + provider + "_" + route + ".csv";

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
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, fl.getAbsolutePath());
            Log.i(TAG, "DATEI "+ fl.getName()+" WURDE ERSTELLT");
            return fl;
        }
        else{
            Log.i(TAG, "Versuche eine neue Timestamp Datei anzulegen");
            final String OUTPUT_DIR = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator
                    + "LoMoPraktikum";
            final String OUTPUT_FILE_PATH = OUTPUT_DIR + File.separator + timeStamp + "_" + provider + "_" + route + ".timestamp.csv";

            File folder = new File(Constants.OUTPUT_DIR);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File fl = new File(OUTPUT_FILE_PATH);
            try {
                ICSVWriter writer = new CSVWriterBuilder(new FileWriter(fl)).build();
                writer.writeNext(new String[]{""});
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, fl.getAbsolutePath());
            Log.i(TAG, "DATEI "+ fl.getName()+" WURDE ERSTELLT");
            return fl;
        }
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
