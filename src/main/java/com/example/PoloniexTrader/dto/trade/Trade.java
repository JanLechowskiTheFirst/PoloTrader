package com.example.PoloniexTrader.dto.trade;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class Trade {
    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    private BigDecimal rate;
    private BigDecimal total;
    private BigInteger tradeID;
    private String type;
}
