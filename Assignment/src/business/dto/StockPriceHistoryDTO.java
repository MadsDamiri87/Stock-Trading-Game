package business.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockPriceHistoryDTO(UUID stockPriceHistId,
                                   String stockSymbol,
                                   BigDecimal price,
                                   Instant timestamp)
{
}
