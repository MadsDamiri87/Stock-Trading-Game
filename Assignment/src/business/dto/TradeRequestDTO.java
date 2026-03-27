package business.dto;

import java.util.UUID;

public record TradeRequestDTO(UUID portfolieId,
                              String stockSymbol,
                              int quantity)
{

}
