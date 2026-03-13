package presentation.listeners;

import business.services.StockUpdateListener;
import entities.Stock;

public class StockPresentationListener implements StockUpdateListener
{

  @Override public void onStockUpdated(Stock stock)
  {
    System.out.println("UI update -> " + stock.getSymbol() + " | Price: "
                           + stock.getCurrentPrice() + " | State: "
                           + stock.getCurrentState());
  }
}
