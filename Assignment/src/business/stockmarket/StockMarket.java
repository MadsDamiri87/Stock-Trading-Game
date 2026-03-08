package business.stockmarket;

import business.stockmarket.simulation.LiveStock;
import entities.Stock;
import entities.StockPriceHistory;
import persistence.interfaces.StockPriceHistoryDAO;
import shared.logging.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StockMarket
{

  private static StockMarket instance;
  private final List<LiveStock> liveStocks;
  private final Logger logger;
  private final StockPriceHistoryDAO stockPriceHistoryDAO;

  private StockMarket(StockPriceHistoryDAO stockPriceHistoryDAO)
  {
    this.liveStocks           = new ArrayList<>();
    this.logger               = Logger.getInstance();
    this.stockPriceHistoryDAO = stockPriceHistoryDAO;
  }

  public static StockMarket getInstance(
      StockPriceHistoryDAO stockPriceHistoryDAO)
  {
    if (instance == null)
    {
      instance = new StockMarket(stockPriceHistoryDAO);
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

      String logMessage = String.format("Stock: %s | Price: %.2f | State: %s",
                                        liveStock.getStockSymbol(),
                                        liveStock.getCurrentPrice(),
                                        liveStock.getCurrentStateName());

      logger.log("Info", logMessage);

      StockPriceHistory history = new StockPriceHistory(UUID.randomUUID(),
                                                        liveStock.getStockSymbol(),
                                                        BigDecimal.valueOf(
                                                            liveStock.getCurrentPrice()),
                                                        Instant.now());

      stockPriceHistoryDAO.create(history);
    }
  }

  public List<LiveStock> getLiveStocks()
  {
    return liveStocks;
  }
}
