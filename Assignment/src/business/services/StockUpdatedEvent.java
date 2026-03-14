package business.services;

import java.math.BigDecimal;

public record StockUpdatedEvent(String symbol,
                                BigDecimal currentPrice,
                                String currentState)
{
}