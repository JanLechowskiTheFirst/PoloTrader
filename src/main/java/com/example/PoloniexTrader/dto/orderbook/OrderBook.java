package com.example.PoloniexTrader.dto.orderbook;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderBook {
    private List<Object> asks;
    private List<Object> bids;
    private boolean isFrozen;
    private long seq;
}
