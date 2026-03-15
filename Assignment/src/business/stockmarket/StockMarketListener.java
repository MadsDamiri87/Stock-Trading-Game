package business.stockmarket;


public interface StockMarketListener
{
  void onStockUpdated(StockMarketUpdateEvent event);

}
