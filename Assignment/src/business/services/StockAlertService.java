package business.services;

import business.stockmarket.StockMarketListener;
import business.stockmarket.simulation.LiveStock;
import persistence.interfaces.UnitOfWork;
import shared.logging.Logger;

public class StockAlertService implements StockMarketListener
{
  private final Logger logger;

  public StockAlertService()
  {
    this.logger = Logger.getInstance();
  }

  @Override public void onStockUpdated(LiveStock liveStock)
  {
    String symbol = liveStock.getStockSymbol();
    double price = liveStock.getCurrentPrice();
    String state = liveStock.getCurrentStateName();

    if (state.equalsIgnoreCase("Bankrupt"))
    {
      logger.log("Alert", "Stock: " + symbol + " er Bankrupt");
    }
    if (state.equalsIgnoreCase("Reset"))
    {
      logger.log("Alert",
                 "Stock: " + symbol + " er blevet reset og kan købes igen");
    }
    if (price > 180)
    {
      logger.log("Alert", "Stock: " + symbol + " er meget høj: " + price);
    }

    if (price < 50)
    {
      logger.log("Alert", "Stock " + symbol + " er meget lav: " + price);

    }
  }
}
