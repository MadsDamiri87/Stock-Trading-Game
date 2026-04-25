package business.services;

import business.dto.StockPriceHistoryDTO;
import business.services.interfaces.StockPriceHistoryInterface;
import business.services.mapping.StockPriceHistoryMapper;
import persistence.interfaces.StockPriceHistoryDAO;

import java.util.List;

public class StockPriceHistoryService implements StockPriceHistoryInterface
{
  private final StockPriceHistoryDAO stockPriceHistoryDAO;

  public StockPriceHistoryService(StockPriceHistoryDAO stockPriceHistoryDAO)
  {
    this.stockPriceHistoryDAO = stockPriceHistoryDAO;
  }

  @Override public List<StockPriceHistoryDTO> getHistoryForStock(String symbol)
  {
    return stockPriceHistoryDAO.getByStockSymbol(symbol)
                               .stream()
                               .map(StockPriceHistoryMapper::toDTO)
                               .toList();
  }
}
