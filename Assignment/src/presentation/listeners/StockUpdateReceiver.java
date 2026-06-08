package presentation.listeners;

import business.stockmarket.StockMarketUpdateEvent;

public interface StockUpdateReceiver
{
  void onStockUpdateViewModel(StockMarketUpdateEvent event);
}
