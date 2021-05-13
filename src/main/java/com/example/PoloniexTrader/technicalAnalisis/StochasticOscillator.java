package com.example.PoloniexTrader.technicalAnalisis;

import java.util.Comparator;
import java.util.List;

public class StochasticOscillator {

    long digits;

    public double calculateStochasticOscilatorValue(
            long period, List<Double> subList, long digits) {
        this.digits = digits;
        if (period == 0) {
            period = 14;
        }
        subList = subList.subList(subList.size() - (int) period, subList.size());
        double max = subList.stream().max(Comparator.comparing(x -> x)).orElse(0d);
        double min = subList.stream().min(Comparator.comparing(x -> x)).orElse(0d);
        return ((subList.get(subList.size() - 1) - min) / (max - min)) * 100;
    }
}
