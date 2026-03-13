package business.stockmarket;

import business.stockmarket.simulation.LiveStock;

public interface StockMarketListener
{
  void onStockUpdated(LiveStock liveStock);

}
