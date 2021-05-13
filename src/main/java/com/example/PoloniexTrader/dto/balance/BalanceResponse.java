package com.example.PoloniexTrader.dto.balance;

import com.example.PoloniexTrader.service.model.Currency;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceResponse {
    private String BTC;
    private String USDT;

    public BalanceResponse(String BTC, String USDT) {
        this.BTC = BTC;
        this.USDT = USDT;
    }

    public double getCurrencyValue(Currency currency) {
        if (currency == Currency.BTC) {
            return Double.parseDouble(getBTC());
        }
        if (currency == Currency.USDT) {
            return Double.parseDouble(getUSDT());
        }
        return 0;
    }

    public String getBTC() {
        return BTC;
    }

    public String getUSDT() {
        return USDT;
    }
}
