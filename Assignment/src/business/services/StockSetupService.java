package business.services;

import entities.Stock;
import persistence.interfaces.StockDAO;
import persistence.interfaces.UnitOfWork;
import shared.logging.Logger;

import java.math.BigDecimal;
import java.util.Optional;

public class StockSetupService
{
  private final Logger logger;
  private final UnitOfWork uow;
  private final StockDAO stockDAO;

  public StockSetupService(UnitOfWork uow, StockDAO stockDAO)
  {
    this.logger   = Logger.getInstance();
    this.uow      = uow;
    this.stockDAO = stockDAO;
  }

  public Stock getOrCreateStock(String symbol, String name, BigDecimal price,
                                String state)
  {
    try
    {
      uow.beginTransaction();
      Optional<Stock> optionalStock = stockDAO.getBySymbol(symbol);

      if (optionalStock.isPresent())
      {
        logger.log("Info", "Stock " + symbol + " findes allerede - ");
        uow.rollback();
        return optionalStock.get();

      }
      logger.log("Info", "Opretter ny stock: " + symbol + " from class: ");
      Stock stock = new Stock(symbol, name, price, state);
      stockDAO.create(stock);

      uow.commit();

      logger.log("Info", "Stock " + symbol + " blev oprettet");

      return stock;

    }
    catch (Exception e)
    {
      uow.rollback();
      logger.log("Error", "Fejl i StockSetupService: " + e.getMessage());
      throw new RuntimeException("Fejl ved oprettelse af stock", e);
    }

  }
}