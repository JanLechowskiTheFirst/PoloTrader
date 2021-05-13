package com.example.PoloniexTrader.bot;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.example.PoloniexTrader.config.PoloniexProperties;
import com.example.PoloniexTrader.dto.balance.BalanceResponse;
import com.example.PoloniexTrader.dto.chartData.Candle;
import com.example.PoloniexTrader.dto.orderbook.Order;
import com.example.PoloniexTrader.dto.orderbook.OrderBookModel;
import com.example.PoloniexTrader.dto.trade.TradeResult;
import com.example.PoloniexTrader.service.PoloniexService;
import com.example.PoloniexTrader.service.model.Command;
import com.example.PoloniexTrader.service.model.CurrencyPair;
import com.example.PoloniexTrader.service.model.Decision;
import com.example.PoloniexTrader.technicalAnalisis.MacdStrategy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PoloniexBot {

    private final PoloniexService poloniexService;
    private final PoloniexProperties poloniexProperties;

    private static final Logger LOGGER = LoggerFactory.getLogger(PoloniexBot.class);
    private static final Logger TRANSACTION_LOGGER = LoggerFactory.getLogger("transaction");
    private LocalDateTime lastCandleDate = null;

    public PoloniexBot(PoloniexService poloniexService, PoloniexProperties poloniexProperties) {
        this.poloniexService = poloniexService;
        this.poloniexProperties = poloniexProperties;
    }

    @Scheduled(fixedRate = 20000)
    public void trade() {
        final CurrencyPair currencyPair = poloniexProperties.getCurrencyPair();
        List<Candle> candleList =
                poloniexService.getTradeHistory(
                        LocalDateTime.now().minusDays(60), LocalDateTime.now(), currencyPair.name());
        if(isFreshCandleOut(candleList)) {
            candleList.remove(candleList.size()-1);
            MacdStrategy macdStrategy =
                    MacdStrategy.builder()
                            .shortPeriod(12)
                            .longPeriod(26)
                            .signal(9)
                            .closePrices(
                                    candleList.stream()
                                            .map(Candle::getClose)
                                            .collect(Collectors.toList()))
                            .build();
            executeTrade(macdStrategy.generateSignal(), currencyPair);
        }
    }

    private void executeTrade(final Decision decision, final CurrencyPair currencyPair) {
        final BalanceResponse balances = poloniexService.getBalanceForCurrency();
        final double buyBalance = balances.getCurrencyValue(currencyPair.getBuy());
        final double sellBalance = balances.getCurrencyValue(currencyPair.getSell());
        if (((decision == Decision.SELL && sellBalance > currencyPair.getSell().getMinimum())
                || (decision == Decision.BUY && buyBalance > currencyPair.getBuy().getMinimum()))) {
            final BigDecimal rate = calculateRate(decision, currencyPair);
            final BigDecimal amount =
                    calculateAmount(
                            decision,
                            rate,
                            buyBalance,
                            sellBalance,
                            poloniexProperties.getBudgetBalancePercent());
            if (!rate.equals(BigDecimal.ZERO) && !amount.equals(BigDecimal.ZERO)) {
                final TradeResult result =
                        poloniexService.trade(
                                mapDecisionToCommand(decision), currencyPair, rate, amount);
                if (isNull(result.getResultingTrades()) || result.getResultingTrades().isEmpty()) {
                    LOGGER.error(
                            "No trades were made for decision {} amount {} rate {}",
                            decision,
                            amount,
                            rate);
                } else {
                    TRANSACTION_LOGGER.info(
                            "Transaction made, direction {}, amount {}, rate {}, resulting trades sum amount {}",
                            decision,
                            amount,
                            rate,
                            result.getResultingTrades().stream()
                                    .mapToDouble(t -> t.getAmount().doubleValue())
                                    .sum());
                }
            } else {
                LOGGER.error(
                        "Transaction was not commited, becouse amout:{} and rate:{} values",
                        amount,
                        rate);
            }
        }
    }

    private BigDecimal calculateAmount(
            Decision decision,
            BigDecimal rate,
            double buyBalance,
            double sellBalance,
            int buyBalanceLimitPercent) {
        BigDecimal amount = BigDecimal.ZERO;
        if (decision == Decision.BUY && buyBalance > 0.0) {
            amount = BigDecimal.valueOf(buyBalance).divide(rate, 8, RoundingMode.HALF_UP);
            if (buyBalanceLimitPercent <= 100 && buyBalanceLimitPercent >= 1) {
                amount =
                        amount.multiply(BigDecimal.valueOf(buyBalanceLimitPercent))
                                .multiply(BigDecimal.valueOf(0.01));
            }
        }
        if (decision == Decision.SELL && sellBalance > 0.0) {
            amount = BigDecimal.valueOf(sellBalance);
        }
        if (amount.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        if (amount.multiply(rate).compareTo(new BigDecimal("1")) < 0) {
            amount = new BigDecimal("1").divide(rate, 8, RoundingMode.HALF_UP);
            LOGGER.info("amount and rate were to small, increasing amount to {}", amount);
        }
        return amount;
    }

    private BigDecimal calculateRate(Decision decision, CurrencyPair currencyPair) {
        int depth = poloniexProperties.getOrderBookPriceDepth();
        OrderBookModel orderBook = poloniexService.getOrderBook(currencyPair, depth);
        double price = getPriceFromNthLevel(orderBook, decision, depth);
        return BigDecimal.valueOf(price);
    }

    private double getPriceFromNthLevel(
            OrderBookModel orderBookModel, Decision decision, int depth) {
        if (nonNull(orderBookModel)
                && !orderBookModel.getAsks().isEmpty()
                && !orderBookModel.getBids().isEmpty()
                && depth != 0) {
            if (decision == Decision.BUY) {
                List<Order> asks = orderBookModel.getAsks();
                if (asks.size() >= depth - 1) return asks.get(depth - 1).getPrice();
            }
            if (decision == Decision.SELL) {
                List<Order> bids = orderBookModel.getBids();
                if (bids.size() >= depth - 1) return bids.get(depth - 1).getPrice();
            }
        }
        throw new IllegalArgumentException(
                "Order book (including bids and aks), decision or depth are missing");
    }

    public Command mapDecisionToCommand(Decision decision) {
        if (decision == Decision.BUY) {
            return Command.BUY;
        } else if (decision == Decision.SELL) {
            return Command.SELL;
        } else return null;
    }

    private boolean isFreshCandleOut(final List<Candle> candleList) {
        final LocalDateTime lastChartCandleDate = candleList.get(candleList.size()-1).getDate();
        if(!lastChartCandleDate.equals(lastCandleDate)) {
            lastCandleDate = lastChartCandleDate;
            return true;
        }
        else {
            return false;
        }
    }
}
