package com.example.praktikum1.statistics;

import java.util.ArrayList;


public class DataList extends ArrayList<Float> {

    public double median() {
        if(super.isEmpty()) return -1;

        if(super.size() % 2 != 0) {
            return super.get(super.size() / 2 + 1);
        }
        else {
            return (super.get(super.size() / 2) + super.get(super.size() / 2 + 1)) / 2.0;
        }
    }
}
