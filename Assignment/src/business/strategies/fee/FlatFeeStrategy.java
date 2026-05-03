package business.strategies.fee;

import java.math.BigDecimal;

public class FlatFeeStrategy implements FeeCalculationStrategy
{
  private final BigDecimal fee;

  public FlatFeeStrategy(BigDecimal fee)
  {
    this.fee = fee;
  }

  @Override public BigDecimal calculateFee(BigDecimal stockPrice, int quantity)
  {
    return fee;
  }
}
