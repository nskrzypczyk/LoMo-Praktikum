package com.example.praktikum1;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.var;

@Getter @Setter @RequiredArgsConstructor @NoArgsConstructor
public class Position {

    @NonNull private String timeStamp;
    @NonNull private double latitude;
    @NonNull private double longitude;
    @NonNull private double altitude;

    /**
     * Wird vom CSVWriter verwendet, da es im Androi-Java
     * nicht alle Beans gibt... :s
     * @return Spalten der CSV-Datei
     */
    public String[] toStringArray() {
        // Punkt durch Komma ersetzen, damit die Zahlen von
        // Excel und co. korrekt interpretiert werden.
        return new String[] {
                timeStamp,
                (latitude + "").replace('.', ','),
                (longitude + "").replace('.', ','),
                (altitude + "").replace('.', ',')
        };
    }

    public void toStringArrayList(){
        ArrayList<String> representation = new ArrayList<>();
        representation.add(timeStamp);
        representation.add(latitude+"");
        representation.add(longitude+"");
    }
    public HashMap<String,String> toHashMap(){
       HashMap<String, String> hashMap = new HashMap<>();
       hashMap.put("timeStamp",timeStamp);
       hashMap.put("latitude", latitude+"");
       hashMap.put("longitude", longitude+"");
       return hashMap;
    }

}

