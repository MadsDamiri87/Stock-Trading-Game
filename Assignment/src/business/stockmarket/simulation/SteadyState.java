package business.stockmarket.simulation;

import java.util.Random;

public class SteadyState implements LiveStockState
{

  private static final Random random = new Random();

  @Override public double calculatePriceChange(LiveStock liveStock)
  {
    double change = (random.nextDouble() - 0.5) * 2;
    double changePercentage = change * 100.0;

    if (random.nextDouble() < 0.1)
    {
      liveStock.setState(new RisingState());
    }
    else if (random.nextDouble() < 0.1)
    {
      liveStock.setState(new DecliningState());
    }
    return change;
  }

  @Override public String getStateName()
  {
    return "Steady";
  }

}
