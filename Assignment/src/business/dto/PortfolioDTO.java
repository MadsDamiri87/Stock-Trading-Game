package business.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PortfolioDTO(UUID portfolioId,
                           BigDecimal currentBalance,
                           List<OwnedStockDTO> ownedStocks)
{
}
