package business.strategies.fee;

import java.util.Map;

public class FeeStrategySelector
{
  private final FeeStrategyProvider feeStrategyProvider;
  private final Map<String, FeeCalculationStrategy> strategies;

  public FeeStrategySelector(FeeStrategyProvider feeStrategyProvider,
                             Map<String, FeeCalculationStrategy> strategies)
  {
    this.feeStrategyProvider = feeStrategyProvider;
    this.strategies = strategies;
  }

  public void selectStrategy(String name)
  {
    FeeCalculationStrategy strategy = strategies.get(name);

    if (strategy == null)
    {
      throw new IllegalArgumentException("Unknown fee strategy: " + name);
    }

    feeStrategyProvider.setCurrentStrategy(strategy);
  }
}