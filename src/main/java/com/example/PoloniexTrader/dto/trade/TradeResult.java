package com.example.PoloniexTrader.dto.trade;

import com.example.PoloniexTrader.service.model.CurrencyPair;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import lombok.Getter;

@Getter
public class TradeResult {
    private BigInteger orderNumber;
    private List<Trade> resultingTrades;
    private BigDecimal fee;
    private BigInteger clientOrderId;
    private CurrencyPair currencyPair;
}
