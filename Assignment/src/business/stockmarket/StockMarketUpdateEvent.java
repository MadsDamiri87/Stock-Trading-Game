package business.stockmarket;

public record StockMarketUpdateEvent(
    String stockSymbol,
    double currentPrice,
    String currentState
) {}