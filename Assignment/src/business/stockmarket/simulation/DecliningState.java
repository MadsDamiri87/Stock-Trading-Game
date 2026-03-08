package business.stockmarket.simulation;

import java.util.Random;

public class DecliningState implements LiveStockState
{
  private static final Random random = new Random();


  @Override public double calculatePriceChange(LiveStock liveStock)
  {
    double change = random.nextDouble() *

    return 0;
  }

  @Override public String getStateName()
  {
    return "";
  }
}
