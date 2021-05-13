package com.example.PoloniexTrader.dto.orderbook;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderBookModel {
    private List<Order> asks;
    private List<Order> bids;
}
