package com.example.PoloniexTrader.technicalAnalisis;

import com.example.PoloniexTrader.service.model.Decision;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MacdStrategy {
    long shortPeriod;
    long longPeriod;
    long signal;
    List<Double> closePrices;
    int calculationLength;

    private List<Double> calculateEMA(long period, List<Double> series)
            throws IllegalArgumentException {
        List<Double> emaList = new ArrayList<>();
        if (series.size() >= calculationLength) {
            List<Double> periodSeries =
                    series.subList(series.size() - calculationLength, series.size());
            double previusEma =
                    periodSeries.stream().mapToDouble(d -> d.doubleValue()).sum()
                            / calculationLength;
            double currentEma = 0;
            for (Double close : periodSeries) {
                currentEma = (close * factor(period)) + previusEma * (1 - factor(period));
                emaList.add(currentEma);
                previusEma = currentEma;
            }
            return emaList;
        }
        throw new IllegalArgumentException("Series is shorter then period");
    }

    public double calculateSMA(long period, List<Double> series) {
        series = series.subList(series.size() - (int) period, series.size());
        return series.stream().limit(period).mapToDouble(x -> x).sum() / period;
    }

    private double factor(long period) {
        return 2.0 / (period + 1.0);
    }

    private List<Double> calculateMacd() {
        List<Double> longEmaList = calculateEMA(longPeriod, closePrices);
        List<Double> shortEmaList = calculateEMA(shortPeriod, closePrices);
        List<Double> macd = new ArrayList<>();
        for (int i = 0; i < calculationLength; i++) {
            macd.add(shortEmaList.get(i) - longEmaList.get(i));
        }
        return macd;
    }

    private Decision checkForSignal() {
        List<Double> macd = calculateMacd();
        List<Double> signalEMA = calculateEMA(signal, macd);
        List<Double> diff = new ArrayList<>();
        for (int i = 0; i < macd.size(); i++) {
            diff.add(macd.get(i) - signalEMA.get(i));
        }
        if ((diff.get(diff.size() - 1) * diff.get(diff.size() - 2)) < 0) {
            return diff.get(diff.size() - 1) > 0  ? isNotInHorizontalTrend() ? Decision.BUY : Decision.NONE : Decision.SELL;
        }
        return Decision.NONE;
    }

    public Double calculateMacdHistogram() {
        calculationLength = closePrices.size();
        List<Double> macd = calculateMacd();
        List<Double> signalEMA = calculateEMA(signal, macd);
        return macd.get(macd.size() - 1) - signalEMA.get(signalEMA.size() - 1);
    }

    public Decision generateSignal() {
        calculationLength = closePrices.size();
        return checkForSignal();
    }

    private boolean isNotInHorizontalTrend() {
        return Math.abs(calculateSMA(15, closePrices) - closePrices.get(closePrices.size() -1)) > 10 &&
                Math.abs(calculateSMA(15, closePrices) - closePrices.get(closePrices.size() -2)) > 10;
    }
}
