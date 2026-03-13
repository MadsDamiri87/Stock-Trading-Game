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
    listeners.add(listener);
  }

  public void removeListener(StockMarketListener listener)
  {
    listeners.remove(listener);
  }

  void notifyStockUpdate(LiveStock liveStock)
  {
    for (StockMarketListener listener : listeners)
    {
      listener.onStockUpdated(liveStock);
    }
  }

  public void addNewStock(String stockSymbol)
  {
    LiveStock liveStock = new LiveStock(stockSymbol);
    liveStocks.add(liveStock);
    logger.log("Info - ", "Ny LiveStock tilføjet: " + stockSymbol);
  }

  public void addExistingStock(Stock stock)
  {
    LiveStock liveStk = new LiveStock(stock.getSymbol());
    liveStocks.add(liveStk);

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
      notifyStockUpdate(liveStock);
    }
  }

  public List<LiveStock> getLiveStocks()
  {
    return liveStocks;
  }
}
