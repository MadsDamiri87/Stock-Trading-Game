package presentation.listeners;

import business.services.StockUpdateListener;
import business.services.StockUpdatedEvent;

public class StockPresentationListener implements StockUpdateListener
{
  @Override public void onStockUpdated(StockUpdatedEvent event)
  {
    System.out.println(
        "UI update: " + event.symbol() + " | Price: " + event.currentState()
            + " | State: " + event.currentState());
  }
}
