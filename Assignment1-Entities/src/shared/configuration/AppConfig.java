package shared.configuration;

import java.math.BigDecimal;

public class AppConfig
{

  private static AppConfig instance;

  private final int startingBalance;
  private final BigDecimal transactionFee;
  private final int updateFrequencyInMs;
  private final BigDecimal stockResetValue;

  private AppConfig()
  {
    this.startingBalance     = 10000;
    this.transactionFee      = BigDecimal.valueOf(2.0);
    this.updateFrequencyInMs = 50;
    this.stockResetValue     = BigDecimal.valueOf(100);
  }

  public static synchronized AppConfig getInstance()
  {
    if (instance == null)
    {
      instance = new AppConfig();
    }
    return instance;
  }

  public int getStartingBalance()
  {
    return startingBalance;
  }

  public BigDecimal getTransactionFee()
  {
    return transactionFee;
  }

  public int getUpdateFrequencyInMs()
  {
    return updateFrequencyInMs;
  }

  public BigDecimal getStockResetValue()
  {
    return stockResetValue;
  }
}
