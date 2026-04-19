package presentation.listeners;

import business.stockmarket.StockMarketUpdateEvent;

public interface StockUpdateReceiver
{
  void onStockUpdate(StockMarketUpdateEvent event);
}
