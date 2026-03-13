package business.services;

import business.stockmarket.StockMarketListener;
import business.stockmarket.simulation.LiveStock;
import entities.OwnedStock;
import persistence.fileimplementation.OwnedStockFileDAO;
import persistence.interfaces.OwnedStockDAO;
import persistence.interfaces.UnitOfWork;
import shared.logging.Logger;

import java.util.List;

public class StockBankruptService implements StockMarketListener
{

  private final Logger logger;
  private final UnitOfWork uow;
  private final OwnedStockDAO ownedStockDAO;

  public StockBankruptService(UnitOfWork uow,
                              OwnedStockDAO ownedStockDAO)
  {
    this.logger            = Logger.getInstance();
    this.uow               = uow;
    this.ownedStockDAO = ownedStockDAO;
  }

  @Override public void onStockUpdated(LiveStock liveStock)
  {
    if (!liveStock.getCurrentStateName().equalsIgnoreCase("Bankrupt"))
    {
      return;
    }

    logger.log("Info", "Stock " + liveStock.getStockSymbol()
        + " er bankrupt. Tjekker OwnedStock.");
    try
    {
      uow.beginTransaction();

      List<OwnedStock> ownedStocks = ownedStockDAO.getAll();
      boolean found = false;

      for (OwnedStock ownedStock : ownedStocks)
      {
        if (ownedStock.getStockSymbol()
                      .equalsIgnoreCase(liveStock.getStockSymbol()))
        {
          found = true;
          logger.log("Info",
                     "OwnedStock slettet for: " + liveStock.getStockSymbol());

        }
      }
      if (!found)
      {
        logger.log("Info", "Ingen OwnedStock fundet for bankrupt stock: "
            + liveStock.getStockSymbol());
      }

      uow.commit();
    }
    catch (Exception e)
    {
      uow.rollback();
      logger.log("Error", "Fejl i StockBankruptService: " + e.getMessage());
      throw new RuntimeException("Fejl ved håndtering af bankrupt stock", e);
    }
  }
}
