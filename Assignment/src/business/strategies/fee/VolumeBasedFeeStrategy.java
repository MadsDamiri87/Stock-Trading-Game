package business.strategies.fee;

import java.math.BigDecimal;

public class VolumeBasedFeeStrategy implements FeeCalculationStrategy
{
  private final BigDecimal feePerShare;

  public VolumeBasedFeeStrategy(BigDecimal feePerShare)
  {
    this.feePerShare = feePerShare;
  }

  @Override public BigDecimal calculateFee(BigDecimal stockPrice, int quantity)
  {
    return feePerShare.multiply(BigDecimal.valueOf(quantity));
  }
}
