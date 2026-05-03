package shared.configuration;

import java.math.BigDecimal;

public class AppConfig
{

  private static AppConfig instance;

  private final int startingBalance;
  private final int updateFrequencyInMs;
  private final BigDecimal stockResetValue;

  private AppConfig()
  {
    this.startingBalance     = 100;
    this.updateFrequencyInMs = 1000;
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

  public int getUpdateFrequencyInMs()
  {
    return updateFrequencyInMs;
  }

  public BigDecimal getStockResetValue()
  {
    return stockResetValue;
  }
}
