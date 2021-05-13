package com.example.PoloniexTrader.simulator;

import com.example.PoloniexTrader.dto.chartData.Candle;
import com.example.PoloniexTrader.service.PoloniexService;
import com.example.PoloniexTrader.service.model.CurrencyPair;
import com.example.PoloniexTrader.service.model.Decision;
import com.example.PoloniexTrader.technicalAnalisis.MacdStrategy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MacdStrategyTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MacdStrategyTest.class);
    private final PoloniexService poloniexService;
    private List<Candle> chart;
    private double moneyUSDT = 10000;
    private double bitcoin = 0;
    private double lowest = moneyUSDT;
    private double highest = moneyUSDT;

    public MacdStrategyTest(PoloniexService poloniexService) {
        this.poloniexService = poloniexService;
    }

    public void testStrategy() {
        int testingDays = 300;
        int loopSize = 200;
        chart =
                poloniexService.getTradeHistory(
                        LocalDateTime.now().minusDays(testingDays),
                        LocalDateTime.now(),
                        CurrencyPair.USDT_BTC.name());
        List<Double> closePrices =
                chart.stream().map(Candle::getClose).collect(Collectors.toList());
        for (int counter = loopSize; counter < closePrices.size() - 1; counter++) {
            List<Double> loopClosePrices = closePrices.subList(counter - loopSize, counter);
            MacdStrategy macdStrategy =
                    MacdStrategy.builder()
                            .shortPeriod(12)
                            .longPeriod(26)
                            .signal(9)
                            .closePrices(loopClosePrices)
                            .build();
            executeTestTrade(macdStrategy.generateSignal(), counter);
        }
        LOGGER.info("USDT {} BITCOIN {} LOWEST {} HEIGHTS {}", moneyUSDT, bitcoin, lowest, highest);
    }

    private void executeTestTrade(Decision decision, int counter) {
        if (decision != Decision.NONE) {
            checkBankruptcy();
            if (decision == Decision.BUY && moneyUSDT != 0) {
                bitcoin = moneyUSDT / getClosePrice(counter);
                moneyUSDT = 0;
            }
            if (decision == Decision.SELL && bitcoin != 0) {
                moneyUSDT = bitcoin * getClosePrice(counter);
                bitcoin = 0;
                trackExtremes(moneyUSDT);
                LOGGER.info("Total after sold {}", moneyUSDT);
            }
        }
    }

    private double getClosePrice(int counter) {
        return chart.get(counter).getClose();
    }

    private void checkBankruptcy() {
        if (bitcoin == 0 && moneyUSDT == 0) {
            LOGGER.warn("No more funds left");
            LOGGER.info(
                    "USDT {} BITCOIN {} LOWEST {} HEIGHTS {}", moneyUSDT, bitcoin, lowest, highest);
            System.exit(0);
        }
    }

    private void trackExtremes(double result) {
        if (result < lowest) {
            lowest = result;
        }
        if (result > highest) {
            highest = result;
        }
    }
}
