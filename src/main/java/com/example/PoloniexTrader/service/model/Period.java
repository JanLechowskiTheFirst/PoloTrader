package com.example.PoloniexTrader.service.model;

public enum Period {
    FIVE_MINUTES(300),
    FIFTEEN_MINUTES(900),
    THIRTY_MINUTES(1800),
    TWO_HOURS(7200),
    FOUR_HOURS(14400),
    DAY(86400);

    private int period;

    Period(int period) {
        this.period = period;
    }

    public int getPeriodString() {
        return period;
    }
}
