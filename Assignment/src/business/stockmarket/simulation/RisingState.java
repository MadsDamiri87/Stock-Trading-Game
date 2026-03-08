package business.stockmarket.simulation;

import java.util.Random;

public class RisingState implements LiveStockState
{
  private static final Random random = new Random();

  @Override
  public double calculatePriceChange(LiveStock liveStock)
  {
    double change;

    if (random.nextDouble() < 0.8)
    {
      change = random.nextDouble() * 5;
    }
    else
    {
      change = -(random.nextDouble() * 2);
    }

    double roll = random.nextDouble();

    if (roll < 0.2)
    {
      liveStock.setState(new DecliningState());
    }
    else if (roll < 0.3)
    {
      liveStock.setState(new SteadyState());
    }

    return change;
  }

  @Override
  public String getStateName()
  {
    return "Rising";
  }
}