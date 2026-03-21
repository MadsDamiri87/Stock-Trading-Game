package business.dto;

import java.math.BigDecimal;

public record TradeResultDTO(String stockSymbol,
                             String type,
                             int quantity,
                             BigDecimal pricePerShare,
                             BigDecimal fee,
                             BigDecimal totalAmount,
                             BigDecimal newBalance)
{
}
