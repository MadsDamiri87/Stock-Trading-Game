package business.stockmarket.simulation;

import java.util.Random;

public class RisingState implements LiveStockState
{
  private static final Random random = new Random();

  @Override public double calculatePriceChange(LiveStock liveStock)
  {
    double change = random.nextDouble() * 5;

    if (random.nextDouble() < 0.2){
      liveStock.setState(new DecliningState());
    }
    else if (random.nextDouble() < 0.3)
    {
      liveStock.setState(new SteadyState());
    }
    return change;
  }

  @Override public String getStateName()
  {
    return "Rising";
  }
}
