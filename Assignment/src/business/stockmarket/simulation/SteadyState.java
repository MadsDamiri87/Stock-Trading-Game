package business.stockmarket.simulation;

public class SteadyState implements LiveStockState
{
  @Override public double calculatePriceChange(LiveStock liveStock)
  {
    return 0.5;
  }

  @Override public String getStateName()
  {
    return "Steady";
  }
}
