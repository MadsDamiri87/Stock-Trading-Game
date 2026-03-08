package business.stockmarket.simulation;

import shared.configuration.AppConfig;

public class ResetState implements LiveStockState
{
  @Override public double calculatePriceChange(LiveStock liveStock)
  {
    liveStock.setCurrentPrice(AppConfig.getInstance().getStartingBalance());
    liveStock.setState(new SteadyState());
    return 0;
  }

  @Override public String getStateName()
  {
    return "Reset";
  }
}
