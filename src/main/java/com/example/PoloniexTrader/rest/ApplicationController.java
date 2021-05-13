package com.example.PoloniexTrader.rest;

import com.example.PoloniexTrader.config.PoloniexProperties;
import com.example.PoloniexTrader.service.PoloniexService;
import com.example.PoloniexTrader.service.model.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);
    private static final String PASSWORD = "Dire<>12";
    private final PoloniexService service;
    private final PoloniexProperties poloniexProperties;

    public ApplicationController(PoloniexService service, PoloniexProperties poloniexProperties) {
        this.service = service;
        this.poloniexProperties = poloniexProperties;
    }


    @GetMapping("/health")
    public String closeAllBots() {
        return "<div style='width:100%; height: 100%;display:flex;background-color: #222831'><h1 style='margin: auto; font-size:100;color: lightcoral'>Alive bitch!</h1></div>";
    }

    @GetMapping("/stop/{pass}")
    public void stopProgram(@PathVariable String pass) {
        if (PASSWORD.equals(pass)) {
            LOGGER.error("Stopping by user...");
            System.exit(0);
        }
    }

    @GetMapping("/balance/{pass}")
    public double[] balance(@PathVariable String pass) {
        if (PASSWORD.equals(pass)) {
            final CurrencyPair currencyPair = poloniexProperties.getCurrencyPair();
            double[] balanceArray = new double[2];
            double buyBalance =
                    service.getBalanceForCurrency().getCurrencyValue(currencyPair.getBuy());
            double sellBalance =
                    service.getBalanceForCurrency().getCurrencyValue(currencyPair.getSell());
            balanceArray[0] = buyBalance;
            balanceArray[1] = sellBalance;
            return balanceArray;
        }
        return new double[0];
    }
}
