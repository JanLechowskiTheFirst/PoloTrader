package com.example.PoloniexTrader.service.model;

public enum CurrencyPair {
    USDT_BTC(Currency.USDT, Currency.BTC),
    USDT_TRX(Currency.USDT, Currency.TRX);

    private final Currency buy;
    private final Currency sell;

    CurrencyPair(Currency buy, Currency sell) {
        this.buy = buy;
        this.sell = sell;
    }

    public Currency getBuy() {
        return buy;
    }

    public Currency getSell() {
        return sell;
    }
}
