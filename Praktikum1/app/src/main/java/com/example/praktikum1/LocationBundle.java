package com.example.praktikum1;

import android.location.Location;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter
public class LocationBundle {
    private List<Location> flpHighLocationList;
    private List<Location> flpLowLocationList;
    private List<Location> lmLocationList;
    private List<Location> flagList;
    private List<Date> flpHighFlagTimestampList;
    private List<Date> flpLowFlagTimestampList;
    private List<Date> lmFlagTimestampList;
}
