package business.stockmarket;

import business.stockmarket.simulation.LiveStock;
import entities.Stock;
import shared.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class StockMarket
{

  private static StockMarket instance;
  private final List<LiveStock> liveStocks = new ArrayList<>();
  private final Logger logger = Logger.getInstance();
  private final List<StockMarketListener> listeners = new ArrayList<>();

  private StockMarket()
  {
  }

  public static StockMarket getInstance()
  {
    if (instance == null)
    {
      instance = new StockMarket();
    }
    return instance;
  }

  public void addListener(StockMarketListener listener)
  {
    if (listener == null)
    {
      throw new IllegalArgumentException("Listner cannot be null");
    }
    if (!listeners.contains(listener))
    {
      listeners.add(listener);
    }
  }

  public void removeListener(StockMarketListener listener)
  {
    listeners.remove(listener);
  }

  private void notifyStockUpdate(StockMarketUpdateEvent event)
  {
    for (StockMarketListener listener : listeners)
    {
      try
      {
        listener.onStockUpdated(event);

      }
      catch (Exception e)
      {
        logger.log("Error",
                   "Listener failed for stock " + event.stockSymbol() + ": "
                       + e.getMessage());
      }
    }
  }

  public void addNewStock(String stockSymbol)
  {
    boolean alreadyExists = liveStocks.stream().anyMatch(
        s -> s.getStockSymbol().equalsIgnoreCase(stockSymbol));

    if (alreadyExists)
    {
      logger.log("Info", "LiveStock already exists in market: " + stockSymbol);
      return;
    }

    LiveStock liveStock = new LiveStock(stockSymbol);
    liveStocks.add(liveStock);
    logger.log("Info", "Ny LiveStock tilføjet: " + stockSymbol);
  }

  public void addExistingStock(Stock stock)
  {
    if (stock == null)
    {
      throw new IllegalArgumentException("Stock cannot be null");
    }
    if (stock.getCurrentPrice() == null)
    {
      throw new IllegalArgumentException(
          "Stock price cannot be null for symbol: " + stock.getSymbol());
    }

    boolean alreadyExists = liveStocks.stream().anyMatch(
        s -> s.getStockSymbol().equalsIgnoreCase(stock.getSymbol()));

    if (alreadyExists)
    {
      logger.log("Info",
                 "Existing LiveStock already loaded: " + stock.getSymbol());
      return;
    }

    LiveStock liveStock = new LiveStock(stock.getSymbol(),
                                        stock.getCurrentPrice().doubleValue());

    liveStocks.add(liveStock);

    logger.log("Info", "Eksisterende Stock tilføjet som LiveStock: "
        + stock.getSymbol());
  }

  public void updateAllStocks()
  {
    for (LiveStock liveStock : liveStocks)
    {
      liveStock.updatePrice();

      String logMessage = String.format("Stock: %s | Price: %.2f | State: %s",
                                        liveStock.getStockSymbol(),
                                        liveStock.getCurrentPrice(),
                                        liveStock.getCurrentStateName());

      logger.log("Info", logMessage);

      StockMarketUpdateEvent event = new StockMarketUpdateEvent(
          liveStock.getStockSymbol(), liveStock.getCurrentPrice(),
          liveStock.getCurrentStateName());
      notifyStockUpdate(event);
    }
  }

  public List<LiveStock> getLiveStocks()
  {

    return List.copyOf(liveStocks);
  }
}
