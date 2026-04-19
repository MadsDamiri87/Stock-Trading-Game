package business.services;

import business.stockmarket.StockMarketListener;
import business.stockmarket.StockMarketUpdateEvent;
import entities.OwnedStock;
import persistence.interfaces.OwnedStockDAO;
import persistence.interfaces.UnitOfWork;
import shared.logging.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StockBankruptService implements StockMarketListener
{

  private final Logger logger;
  private final UnitOfWork uow;
  private final OwnedStockDAO ownedStockDAO;
  private final Set<String> handledBankruptStocks = new HashSet<>();

  public StockBankruptService(UnitOfWork uow, OwnedStockDAO ownedStockDAO)
  {
    this.logger        = Logger.getInstance();
    this.uow           = uow;
    this.ownedStockDAO = ownedStockDAO;
  }

  @Override public void onStockUpdated(StockMarketUpdateEvent event)
  {
    String symbol = event.stockSymbol();
    String state = event.currentState();

    if (!state.equalsIgnoreCase("Bankrupt"))
    {
      handledBankruptStocks.remove(symbol);
      return;
    }

    if (handledBankruptStocks.contains(symbol))
    {
      return;
    }

    handledBankruptStocks.add(symbol);

    logger.log("Info", "Stock " + symbol + " er bankrupt. Tjekker OwnedStock.");

    try
    {
      uow.beginTransaction();

      List<OwnedStock> ownedStocks = ownedStockDAO.getAll();
      boolean found = false;

      for (OwnedStock ownedStock : ownedStocks)
      {
        if (ownedStock.getStockSymbol().equalsIgnoreCase(symbol))
        {
          found = true;
          ownedStockDAO.delete(ownedStock.getOwnedStockId());
          logger.log("Info", "OwnedStock slettet for: " + symbol);
        }
      }
      if (!found)
      {
        logger.log("Info", "Ingen OwnedStock fundet for bankrupt stock: " + symbol);
      }

      uow.commit();
    }
    catch (Exception e)
    {
      uow.rollback();
      handledBankruptStocks.remove(symbol);
      logger.log("Error", "Fejl i StockBankruptService: " + e.getMessage());
      throw new RuntimeException("Fejl ved håndtering af bankrupt stock", e);
    }
  }
}
