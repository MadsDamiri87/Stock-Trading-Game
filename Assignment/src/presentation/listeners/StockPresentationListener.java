package presentation.listeners;

import business.stockmarket.StockMarketListener;
import business.stockmarket.StockMarketUpdateEvent;
import javafx.application.Platform;

public class StockPresentationListener implements StockMarketListener
{

  private final StockUpdateReceiver receiver;

  public StockPresentationListener(StockUpdateReceiver receiver)
  {
    this.receiver = receiver;
  }

  @Override public void onStockUpdated(StockMarketUpdateEvent event)
  {
    Platform.runLater(() -> {
      receiver.onStockUpdate(event);
    });

    System.out.println(
        "UI update: " + event.stockSymbol() + " | Price: " + event.currentPrice() + " | State: "
            + event.currentState());
  }
}
