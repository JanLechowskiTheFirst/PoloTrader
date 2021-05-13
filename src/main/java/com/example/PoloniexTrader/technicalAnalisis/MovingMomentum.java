package com.example.PoloniexTrader.technicalAnalisis;

import com.example.PoloniexTrader.service.model.Decision;
import java.util.List;

public class MovingMomentum {

    public Decision generateSignal(
            List<Double> subList,
            int longSMAPeriod,
            int shortSMAPeriod,
            int oscilatorPeriod,
            int macdShortPeriod,
            int macdLongPeriod,
            int macdSignalPeriod,
            long digits) {
        MacdStrategy macdStrategy =
                MacdStrategy.builder()
                        .shortPeriod(macdShortPeriod)
                        .longPeriod(macdLongPeriod)
                        .signal(macdSignalPeriod)
                        .closePrices(subList)
                        .build();
        double sma150 = macdStrategy.calculateSMA(longSMAPeriod, subList);
        double sma20 = macdStrategy.calculateSMA(shortSMAPeriod, subList);
        StochasticOscillator oscilator = new StochasticOscillator();
        double oscilator14 =
                oscilator.calculateStochasticOscilatorValue(oscilatorPeriod, subList, digits);
        double macdHistogram = macdStrategy.calculateMacdHistogram();
        if (sma20 > sma150 && oscilator14 < 20 && macdHistogram > 0) {
            return Decision.BUY;
        } else if (sma20 < sma150 && oscilator14 > 80 && macdHistogram < 0) {
            return Decision.SELL;
        }
        return Decision.NONE;
    }
}
