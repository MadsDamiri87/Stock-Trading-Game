package business.stockmarket.simulation;

import java.util.Random;

public class ResetState implements LiveStockState
{
  private final Random random = new Random();

  @Override public double calculatePriceChange(LiveStock liveStock)
  {
    double minPrice = 100;
    double maxPrice = 200;
    double newRandomStockPrice = minPrice + random.nextDouble() * (maxPrice - minPrice);

    liveStock.setCurrentPrice(newRandomStockPrice);
    liveStock.setState(new SteadyState());
    return 0;
  }

  @Override public String getStateName()
  {
    return "Reset";
  }
}
