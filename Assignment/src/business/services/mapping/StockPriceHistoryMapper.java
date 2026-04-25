package business.services.mapping;

import business.dto.StockPriceHistoryDTO;
import entities.StockPriceHistory;

public class StockPriceHistoryMapper
{
  public static StockPriceHistoryDTO toDTO(StockPriceHistory history)
  {
    return new StockPriceHistoryDTO(history.getStockPriceHistId(), history.getStockSymbolId(),
                                    history.getPrice(), history.getTimestamp());
  }
}
