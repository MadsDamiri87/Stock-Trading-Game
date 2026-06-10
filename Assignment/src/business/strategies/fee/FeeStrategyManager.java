package business.strategies.fee;

public class FeeStrategyManager implements FeeStrategyProvider
{
  private FeeCalculationStrategy currentStrategy;

  public FeeStrategyManager(FeeCalculationStrategy initialStrategy)
  {
    this.currentStrategy = initialStrategy;
  }

  @Override
  public FeeCalculationStrategy getCurrentStrategy()
  {
    return currentStrategy;
  }

  @Override
  public void setCurrentStrategy(
      FeeCalculationStrategy strategy)
  {
    this.currentStrategy = strategy;
  }
}
