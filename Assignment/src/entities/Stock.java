package entities;

import java.math.BigDecimal;

public class Stock {
    private final String symbol;
    private String name;
    private BigDecimal currentPrice;
    private String currentState;

    public Stock(String symbol, String name,
                 BigDecimal currentPrice, String currentState) {

        this.symbol       = symbol;
        this.name         = name;
        this.currentPrice = currentPrice;
        this.currentState = currentState;
    }
}
