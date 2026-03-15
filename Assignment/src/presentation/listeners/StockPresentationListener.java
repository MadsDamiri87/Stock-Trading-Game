package presentation.listeners;

import business.stockmarket.StockMarketListener;
import business.stockmarket.StockMarketUpdateEvent;

public class StockPresentationListener implements StockMarketListener
{
  @Override public void onStockUpdated(StockMarketUpdateEvent event)
  {
    System.out.println(
        "UI update: " + event.stockSymbol() + " | Price: " + event.currentPrice()
            + " | State: " + event.currentState());
  }
}
