package business.services;

import entities.Stock;

public interface StockUpdateListener
{
  void onStockUpdated(Stock stock);

}
