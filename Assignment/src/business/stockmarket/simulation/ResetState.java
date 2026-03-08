package business.stockmarket.simulation;

public class ResetState implements LiveStockState
{
  @Override public double calculatePriceChange(LiveStock liveStock)
  {
    return 0;
  }

  @Override public String getStateName()
  {
    return "";
  }
}
