package business.services;

import business.stockmarket.StockMarketListener;
import business.stockmarket.simulation.LiveStock;
import entities.Stock;
import entities.StockPriceHistory;
import persistence.interfaces.StockDAO;
import persistence.interfaces.StockPriceHistoryDAO;
import persistence.interfaces.UnitOfWork;
import shared.logging.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StockListenerService implements StockMarketListener
{

  private final Logger logger;
  private final UnitOfWork uow;
  private final StockDAO stockDAO;
  private final StockPriceHistoryDAO stockPriceHistoryDAO;
  private final List<StockUpdateListener> listeners = new ArrayList<>();

  public StockListenerService(UnitOfWork uow, StockDAO stockDAO,
                              StockPriceHistoryDAO stockPriceHistoryDAO)
  {
    this.logger               = Logger.getInstance();
    this.uow                  = uow;
    this.stockDAO             = stockDAO;
    this.stockPriceHistoryDAO = stockPriceHistoryDAO;
  }

  public void addListener(StockUpdateListener listener)
  {
    listeners.add(listener);
  }

  public void removeListener(StockUpdateListener listener)
  {
    listeners.remove(listener);
  }

  private void notifyStockUpdated(Stock stock)
  {
    for (StockUpdateListener listener : listeners)
    {
      listener.onStockUpdated(stock);
    }
  }

  @Override public void onStockUpdated(LiveStock liveStock)
  {

    logger.log("Info", "StockListenerService modtog update for: "
        + liveStock.getStockSymbol());

    try
    {
      uow.beginTransaction();

      Optional<Stock> optionalStock = stockDAO.getBySymbol(
          liveStock.getStockSymbol());

      if (optionalStock.isEmpty())
      {
        logger.log("Error",
                   "Stock blev ikke fundet: " + liveStock.getStockSymbol());
        uow.rollback();
        return;
      }

      Stock stock = optionalStock.get();

      stock.setCurrentPrice(BigDecimal.valueOf(liveStock.getCurrentPrice()));
      stock.setCurrentState(liveStock.getCurrentStateName());

      stockDAO.update(stock);

      StockPriceHistory history = new StockPriceHistory(UUID.randomUUID(),
                                                        liveStock.getStockSymbol(),
                                                        BigDecimal.valueOf(
                                                            liveStock.getCurrentPrice()),
                                                        Instant.now());

      stockPriceHistoryDAO.create(history);

      uow.commit();

      logger.log("Info", "Stock " + stock.getSymbol() + " blev opdateret"
          + " og pricehistory blev gemt");

      notifyStockUpdated(stock);
    }
    catch (Exception e)
    {
      uow.rollback();
      logger.log("Error", "Fejl i StockListenerService: " + e.getMessage());
      throw new RuntimeException("Fejl ved opdateringen af stock", e);
    }



  }

}