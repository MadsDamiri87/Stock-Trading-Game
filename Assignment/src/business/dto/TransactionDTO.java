package business.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDTO(String stockSymbol,
                             String type,
                             int quantity,
                             BigDecimal pricePerShare,
                             BigDecimal totalAmount,
                             BigDecimal fee,
                             Instant timestamp)
{
}