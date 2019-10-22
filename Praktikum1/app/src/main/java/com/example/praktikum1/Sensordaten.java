package com.example.praktikum1;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Singular;

@Getter @Setter @RequiredArgsConstructor @NoArgsConstructor
public class Sensordaten {
    @NonNull private double accX,accY,accZ,prox,axisX, axisY, axisZ;
}
