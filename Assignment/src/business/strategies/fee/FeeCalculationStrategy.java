package business.strategies.fee;

import java.math.BigDecimal;

public interface FeeCalculationStrategy
{
  BigDecimal calculateFee(BigDecimal stockPrice, int quantity);

}
