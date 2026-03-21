package business.services.mapping;

import business.dto.OwnedStockDTO;
import business.dto.PortfolioDTO;
import entities.Portfolio;

import java.util.List;

public class PortfolioMapper
{
  public static PortfolioDTO toPortfolioDTO(Portfolio portfolio,
                                            List<OwnedStockDTO> ownedStocks)
  {
    return new PortfolioDTO(portfolio.getPortfolioId(),
                            portfolio.getCurrentBalance(), ownedStocks);
  }
}
