package com.example.praktikum1;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {

    public static String getTimeStamp(Location location){
        Date date = new Date(location.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY-HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }
}
