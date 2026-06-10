package business.strategies.fee;

public interface FeeStrategyProvider
{
  FeeCalculationStrategy getCurrentStrategy();

  void setCurrentStrategy(FeeCalculationStrategy strategy);
}
