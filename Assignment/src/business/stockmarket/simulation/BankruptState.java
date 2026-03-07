package business.stockmarket.simulation;

public class BankruptState implements LiveStockState
{

  @Override public double calculatePriceChange(LiveStock liveStock)
  {
    return 0;
  }

  @Override public String getStateName()
  {
    return "Bankrupt";
  }
}
