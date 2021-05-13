package com.example.PoloniexTrader.dto.orderbook;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Order {
    private double price;
    private double amount;
}
