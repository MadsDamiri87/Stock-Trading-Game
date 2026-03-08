package business.stockmarket;

import persistence.interfaces.StockPriceHistoryDAO;
import shared.configuration.AppConfig;
import shared.logging.Logger;

import java.util.concurrent.ThreadLocalRandom;

public class MarketTickHandler implements Runnable
{
  private final StockMarket stockMarket;
  private final Logger logger;
  private boolean running;
  private StockPriceHistoryDAO stockPriceHistoryDAO;

  public MarketTickHandler(StockMarket stockMarket)
  {
    this.stockMarket = stockMarket;
    this.logger      = Logger.getInstance();
    this.running     = true;

  }

  @Override public void run()
  {
    while (running)
    {
      stockMarket.updateAllStocks();

      logger.log("Info - ", "Markedet blev opdateret");

      int base = AppConfig.getInstance().getUpdateFrequencyInMs();
      int varience = base / 2;
      int freqUpdate =
          ThreadLocalRandom.current().nextInt(-varience, varience + 1) + base;

      try
      {
        System.out.println(freqUpdate);
        Thread.sleep(freqUpdate);
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
