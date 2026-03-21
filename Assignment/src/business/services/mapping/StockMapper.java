package business.services.mapping;

import business.dto.StockDTO;
import entities.Stock;

public class StockMapper
{
  public static StockDTO toStockDTO(Stock stock)
  {
    return new StockDTO(stock.getSymbol(), stock.getName(),
                        stock.getCurrentPrice(), stock.getCurrentState());
  }
}
