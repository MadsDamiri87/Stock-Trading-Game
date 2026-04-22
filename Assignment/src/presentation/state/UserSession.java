package presentation.state;

import java.util.UUID;

public class UserSession
{
  private UUID activePortfolioId;


  public UUID getActivePortfolioId()
  {
    return activePortfolioId;
  }

  public void setActivePortfolioId(UUID activePortfolioId)
  {
    this.activePortfolioId = activePortfolioId;
  }

}