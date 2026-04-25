package business.services.interfaces;

import business.dto.StockPriceHistoryDTO;

import java.util.List;

public interface StockPriceHistoryInterface
{
  List<StockPriceHistoryDTO> getHistoryForStock(String symbol);
}
