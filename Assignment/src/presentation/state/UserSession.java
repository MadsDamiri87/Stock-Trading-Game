package presentation.state;

import java.util.UUID;

public class UserSession
{
  private String traderName;
  private UUID activePortfolioId;

  public String getTraderName()
  {
    return traderName;
  }

  public void setTraderName(String traderName)
  {
    this.traderName = traderName;
  }

  public UUID getActivePortfolioId()
  {
    return activePortfolioId;
  }

  public void setActivePortfolioId(UUID activePortfolioId)
  {
    this.activePortfolioId = activePortfolioId;
  }

}