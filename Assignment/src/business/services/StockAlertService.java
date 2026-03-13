package business.services;

import business.stockmarket.StockMarketListener;
import business.stockmarket.simulation.LiveStock;
import persistence.interfaces.UnitOfWork;
import shared.logging.Logger;

public class StockAlertService implements StockMarketListener
{
  private final Logger logger;
  private final UnitOfWork uow;




  @Override public void onStockUpdated(LiveStock liveStock)
  {


  }
}
