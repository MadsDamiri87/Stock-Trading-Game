package persistence.interfaces;

import entities.StockPriceHistory;

import java.util.List;

public interface StockPriceHistoryDAO
{
  void create(StockPriceHistory history);

  List<StockPriceHistory> getAll();

  List<StockPriceHistory> getByStockSymbol(String symbol);

}
