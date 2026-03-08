package business.stockmarket.simulation;

public class ResetState implements LiveStockState
{
  @Override public double calculatePriceChange(LiveStock liveStock)
  {
    liveStock.setCurrentPrice(100.0);
    liveStock.setState(new SteadyState());
    return 0;
  }

  @Override public String getStateName()
  {
    return "Reset";
  }
}
