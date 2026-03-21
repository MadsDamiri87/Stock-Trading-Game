package business.dto;

import java.math.BigDecimal;

public record StockDTO(String symbol,
                       String name,
                       BigDecimal currentPrice,
                       String currentState)
{
}
