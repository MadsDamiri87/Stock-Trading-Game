package business.stockmarket;

import business.services.GameStateService;
import business.services.interfaces.GameStateServiceInterface;
import shared.configuration.AppConfig;
import shared.logging.Logger;

import java.util.concurrent.ThreadLocalRandom;

public class MarketTickHandler implements Runnable
{
  private final GameStateServiceInterface gameStateService;
  private final Logger logger;
  private boolean running;

  public MarketTickHandler(GameStateServiceInterface gameStateService)
  {
    this.gameStateService = gameStateService;
    this.logger = Logger.getInstance();
    this.running = true;
  }

  @Override
  public void run()
  {
    while (running)
    {
      if (gameStateService.isGameRunning())
      {
        gameStateService.startGame();
        logger.log("Info", "Markedet blev opdateret");
      }

      int base = AppConfig.getInstance().getUpdateFrequencyInMs();
      int variance = base / 2;
      int upperBound = variance + 1;
      int freqUpdate =
          ThreadLocalRandom.current().nextInt(-variance, upperBound) + base;

      try
      {
        if (gameStateService.isGameRunning())
        {
          if (gameStateService instanceof GameStateService concreteService)
          {
            concreteService.updateMarket();
          }
        }

        Thread.sleep(freqUpdate);
      }
      catch (InterruptedException e)
      {
        logger.log("Error", "MarketTickHandler blev afbrudt: " + e.getMessage());
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