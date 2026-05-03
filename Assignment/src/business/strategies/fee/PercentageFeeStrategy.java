package business.strategies.fee;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageFeeStrategy implements FeeCalculationStrategy
{
  private final BigDecimal percentage;

  public PercentageFeeStrategy(BigDecimal percentage)
  {
    this.percentage = percentage;
  }

  @Override public BigDecimal calculateFee(BigDecimal stockPrice, int quantity)
  {
    BigDecimal totalValue = stockPrice.multiply(BigDecimal.valueOf(quantity));

    return totalValue.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
  }
}
