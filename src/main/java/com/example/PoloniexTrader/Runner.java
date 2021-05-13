package com.example.PoloniexTrader;

import com.example.PoloniexTrader.simulator.MacdStrategyTest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Runner implements CommandLineRunner {

    private MacdStrategyTest macdStrategyTest;

    public Runner(MacdStrategyTest macdStrategyTest) {
        this.macdStrategyTest = macdStrategyTest;
    }

    @Override
    public void run(String... args) throws Exception {
//      macdStrategyTest.testStrategy();
//      System.exit(0);
    }
}
