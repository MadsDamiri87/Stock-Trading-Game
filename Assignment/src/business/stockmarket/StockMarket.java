package business.stockmarket;

import business.stockmarket.simulation.LiveStock;
import entities.Stock;
import shared.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class StockMarket
{

  private static StockMarket instance;
  private List<LiveStock> liveStocks;
  private final Logger logger;

  public StockMarket()
  {
    this.liveStocks = new ArrayList<>();
    this.logger     = Logger.getInstance();
  }

  public static StockMarket getInstance()
  {
    if (instance == null)
    {
      instance = new StockMarket();
    }
    return instance;
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

    logger.log("Info - ", "Eksisterende Stock tilføjet som LiveStock: "
        + stock.getSymbol());
  }

  public void updateAllStocks()
  {
    for (LiveStock liveStock : liveStocks)
    {
      liveStock.updatePrice();

      logger.log("Info - ", "Stock: " + liveStock.getStockSymbol() +
          ", Price: " + liveStock.getCurrentPrice() + ", State: " + liveStock.getCurrentStateName());
    }
  }
  public List<LiveStock> getLiveStocks()
  {
    return liveStocks;
  }
  //  Singleton da der kun er et stockmarket
}
