package com.example.PoloniexTrader.service.model;

public enum Currency {
    USDT(1),
    TRX(0.001),
    BTC(0.0001);

    private final double minimum;

    Currency(double minimum) {
        this.minimum = minimum;
    }

    public double getMinimum() {
        return minimum;
    }
}
