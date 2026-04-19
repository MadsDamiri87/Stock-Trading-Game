package business.dto;

import java.util.UUID;

public record TradeRequestDTO(UUID portfolioId,
                              String stockSymbol,
                              int quantity,
                              double tradePrice)
{

}
