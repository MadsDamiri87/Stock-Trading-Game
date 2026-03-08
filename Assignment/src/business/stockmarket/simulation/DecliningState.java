package business.stockmarket.simulation;

import javax.swing.*;
import java.util.Random;

public class DecliningState implements LiveStockState
{
  private static final Random random = new Random();

  @Override public double calculatePriceChange(LiveStock liveStock)
  {
    double change;

    if (random.nextDouble() < 0.6)
    {
      change = -(random.nextDouble() * 5);

    }
    else
    {
      change = random.nextDouble() * 2;
    }
    if (random.nextDouble() < 0.2)
    {
      liveStock.setState(new SteadyState());
    }
    else if (random.nextDouble() < 0.1)
    {
      liveStock.setState(new RisingState());

    }
    return change;
  }

  @Override public String getStateName()
  {
    return "Declining";
  }
}
