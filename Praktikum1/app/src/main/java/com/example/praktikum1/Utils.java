package com.example.praktikum1;

import android.location.Location;
import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import org.apache.commons.collections.bag.SynchronizedSortedBag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import lombok.var;

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

    public static double round(double number, int digits){
        return Math.round(number * Math.pow(10, digits)) / Math.pow(10, digits);
    }

    public static List<Date> readRecordedTimestamps(File file) throws IOException {
        CSVParser CSV_PARSER = new CSVParserBuilder().withIgnoreQuotations(true).withSeparator(';').build();
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


    private static List<Location> readRecordedLocations(File file) throws IOException {
        CSVParser CSV_PARSER = new CSVParserBuilder().withIgnoreQuotations(true).withSeparator(';').build();

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

    public static LocationBundle getLocationBundleFromAllFiles(String route) throws Exception{
        LocationBundle bundle = new LocationBundle();
        CSVParser CSV_PARSER = new CSVParserBuilder().withIgnoreQuotations(true).withSeparator(';').build();

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
        var flagList = entries.stream().map(entry -> {
            Location loc = new Location("flag");
            loc.setLatitude(Double.parseDouble(entry[0]));
            loc.setLongitude(Double.parseDouble(entry[1]));
            return loc;
        }).collect(Collectors.toList());
        bundle.setFlagList(flagList);

        // Dateien mit den Positionen lesen
        Optional<File> opt = files.stream().filter(f -> f.getName().contains(Utils.TYPE_FLP_HIGH)).findFirst();
        if(opt.isPresent() && opt.get().exists()) {
            var flpHighLocationList = Utils.readRecordedLocations(opt.get());
            bundle.setFlpHighLocationList(flpHighLocationList);
        }

        opt = files.stream().filter(f -> f.getName().contains(Utils.TYPE_FLP_LOW)).findFirst();
        if(opt.isPresent() && opt.get().exists()) {
            bundle.setFlpLowLocationList(readRecordedLocations(opt.get()));
        }

        opt = files.stream().filter(f -> f.getName().contains(Utils.TYPE_LM_GPS)).findFirst();
        if(opt.isPresent() && opt.get().exists()) {
            bundle.setLmLocationList(readRecordedLocations(opt.get()));
        }

        // Die Timestamps für die Flags holen
        String flpHighTimestampFilePattern = Utils.TYPE_FLP_HIGH + "_" + selectedRoute + ".timestamp.csv";
        opt = files.stream().filter(f -> f.getName().contains(flpHighTimestampFilePattern)).findFirst();
        if(opt.isPresent() && opt.get().exists()) {
            bundle.setFlpHighFlagTimestampList(readRecordedTimestamps(opt.get()));
        }

        String flpLowTimestampFilePattern = Utils.TYPE_FLP_LOW + "_" + selectedRoute + ".timestamp.csv";
        opt = files.stream().filter(f -> f.getName().contains(flpLowTimestampFilePattern)).findFirst();
        if(opt.isPresent() && opt.get().exists()) {
            bundle.setFlpLowFlagTimestampList(readRecordedTimestamps(opt.get()));
        }

        String lmTimestampFilePattern = Utils.TYPE_LM_GPS + "_" + selectedRoute + ".timestamp.csv";
        opt = files.stream().filter(f -> f.getName().contains(lmTimestampFilePattern)).findFirst();
        if(opt.isPresent() && opt.get().exists()) {
            bundle.setLmFlagTimestampList(readRecordedTimestamps(opt.get()));
        }
        return bundle;
    }

}
