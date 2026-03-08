package business.stockmarket.simulation;

public class BankruptState implements LiveStockState
{

  private int ticksInBankruptState = 0;

  @Override public double calculatePriceChange(LiveStock liveStock)
  {
    ticksInBankruptState++;

    if (ticksInBankruptState >= 10)
    {
      liveStock.setState(new ResetState());
    }
    return 0;
  }

  @Override public String getStateName()
  {
    return "Bankrupt";
  }
}
