package business.stockmarket;

import shared.configuration.AppConfig;
import shared.logging.Logger;

public class MarketTickHandler implements Runnable
{
  private final StockMarket stockMarket;
  private final Logger logger;
  private boolean running;

  public MarketTickHandler()
  {
    this.stockMarket = StockMarket.getInstance();
    this.logger      = Logger.getInstance();
    this.running     = true;

  }

  @Override public void run()
  {
    while (running)
    {
      stockMarket.updateAllStocks();

      logger.log("Info - ", "Markedet blev opdateret");

      try
      {
        Thread.sleep(AppConfig.getInstance().getUpdateFrequencyInMs());
      }
      catch (InterruptedException e)
      {
        logger.log("Error ",
                   "MarketTickerHandler blev afbrudt: " + e.getMessage());
        stopTicks();
        Thread.currentThread().interrupt();
      }
    }
  }

  public void stopTicks()
  {
    running = false;
  }
}
