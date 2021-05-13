package com.example.PoloniexTrader.config;

import com.example.PoloniexTrader.service.model.CurrencyPair;
import com.example.PoloniexTrader.service.model.Period;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PoloniexProperties {
    @Value("${poloniexAuthentication.secret}")
    private String secret;

    @Value("${poloniexAuthentication.key}")
    private String key;

    @Value("${period}")
    private Period period;

    @Value("${currencyPair}")
    private CurrencyPair currencyPair;

    @Value("${budgetBalancePercent}")
    private int budgetBalancePercent;

    @Value("${orderBookPriceDepth}")
    private int orderBookPriceDepth;

    public String getSecret() {
        return secret;
    }

    public String getKey() {
        return key;
    }

    public Period getPeriod() {
        return period;
    }

    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    public int getBudgetBalancePercent() {
        return budgetBalancePercent;
    }

    public int getOrderBookPriceDepth() {
        return orderBookPriceDepth;
    }
}
