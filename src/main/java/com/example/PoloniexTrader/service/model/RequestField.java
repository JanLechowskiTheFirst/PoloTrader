package com.example.PoloniexTrader.service.model;

public enum RequestField {
    COMMAND("command"),
    CURRENCY_PAIR("currencyPair"),
    START("start"),
    END("end"),
    RATE("rate"),
    AMOUNT("amount"),
    NONCE("nonce"),
    PERIOD("period"),
    FILL_OR_KILL("fillOrKill"),
    IMMEDIATE_OR_CANCEL("immediateOrCancel"),
    DEPTH("depth");

    private String query;

    RequestField(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
