package com.example.PoloniexTrader.dto.chartData;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ChartData {
    private long date;
    private double high;
    private double low;
    private double open;
    private double close;
    private double volume;
    private double quoteVolume;
    private double weightedAverage;
}
