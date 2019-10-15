package com.example.praktikum1;

import android.os.Environment;

import java.io.File;

public final class Constants {
    public static final String OUTPUT_DIR = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "LoMoPraktikum";
    public static final String GPS_OUTPUT_FILE_PATH = OUTPUT_DIR + File.separator + "gps-daten.csv";
}
