package business.services;

import business.stockmarket.StockMarketListener;
import business.stockmarket.StockMarketUpdateEvent;
import entities.Stock;
import entities.StockPriceHistory;
import persistence.interfaces.StockDAO;
import persistence.interfaces.StockPriceHistoryDAO;
import persistence.interfaces.UnitOfWork;
import shared.logging.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class StockListenerService implements StockMarketListener
{

  private final Logger logger;
  private final UnitOfWork uow;
  private final StockDAO stockDAO;
  private final StockPriceHistoryDAO stockPriceHistoryDAO;

  public StockListenerService(UnitOfWork uow, StockDAO stockDAO,
                              StockPriceHistoryDAO stockPriceHistoryDAO)
  {
    this.logger               = Logger.getInstance();
    this.uow                  = uow;
    this.stockDAO             = stockDAO;
    this.stockPriceHistoryDAO = stockPriceHistoryDAO;
  }


  @Override public void onStockUpdated(StockMarketUpdateEvent event)
  {

    logger.log("Info", "StockListenerService modtog update for: "
        + event.stockSymbol());

    try
    {
      uow.beginTransaction();

      Optional<Stock> optionalStock = stockDAO.getBySymbol(
          event.stockSymbol());

      if (optionalStock.isEmpty())
      {
        logger.log("Error",
                   "Stock blev ikke fundet: " + event.stockSymbol());
        uow.rollback();
        return;
      }

      Stock stock = optionalStock.get();

      stock.setCurrentPrice(BigDecimal.valueOf(event.currentPrice()));
      stock.setCurrentState(event.currentState());

      stockDAO.update(stock);

      StockPriceHistory history = new StockPriceHistory(UUID.randomUUID(),
                                                        event.stockSymbol(),
                                                        BigDecimal.valueOf(
                                                            event.currentPrice()),
                                                        Instant.now());

      stockPriceHistoryDAO.create(history);

      uow.commit();

      logger.log("Info", "Stock " + stock.getSymbol() + " blev opdateret"
          + " og pricehistory blev gemt");
    }
    catch (Exception e)
    {
      uow.rollback();
      logger.log("Error", "Fejl i StockListenerService: " + e.getMessage());
      throw new RuntimeException("Fejl ved opdateringen af stock", e);
    }

  }

}