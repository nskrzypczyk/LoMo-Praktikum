package com.example.praktikum1.statistics;

import java.util.ArrayList;


public class DataList extends ArrayList<Float> {

    public double mean() {
        if(super.isEmpty()) return -1;

        double mean = 0;
        for(float error : this) {
            mean += error;
        }
        mean /= super.size();

        return mean;
    }

    public double median() {
        if(super.isEmpty()) return -1;

        if(super.size() % 2 != 0) {
            return super.get(super.size() / 2 + 1);
        }
        else {
            return (super.get(super.size() / 2) + super.get(super.size() / 2 + 1)) / 2.0;
        }
    }

    /**
     * Gibt das k-te Perzentil zurück
     * @param k
     * @return
     */
    public double percentile(int k) {
        if(super.isEmpty()) return -1;

        double p = k / 100.0;
        int index = (int)(super.size() * p) - 1;
        if(index < 0) {
            index = 0;
        }

        return super.get(index);
    }
}
