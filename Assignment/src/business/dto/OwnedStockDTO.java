package business.dto;

public record OwnedStockDTO(
    String stockSymbol,
    int numberOfShares,
    double lastBuyPrice)
{}
