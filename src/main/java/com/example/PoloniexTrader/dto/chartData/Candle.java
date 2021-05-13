package com.example.PoloniexTrader.dto.chartData;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Candle {
    private LocalDateTime date;
    private double high;
    private double low;
    private double open;
    private double close;
}
