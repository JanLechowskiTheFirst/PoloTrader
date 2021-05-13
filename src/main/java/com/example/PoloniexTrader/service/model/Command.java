package com.example.PoloniexTrader.service.model;

public enum Command {
    RETURN_CHART_DATA("returnChartData"),
    RETURN_BALANCES("returnBalances"),
    RETURN_ORDER_BOOK("returnOrderBook"),
    GET_CURRENCIES("returnCurrencies"),
    BUY("buy"),
    SELL("sell");

    private String poloniexCommand;

    Command(String poloniexCommand) {
        this.poloniexCommand = poloniexCommand;
    }

    public String getPoloniexCommand() {
        return poloniexCommand;
    }
}
