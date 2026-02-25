package entities;

import java.util.UUID;

public class OwnedStock
{

  private final UUID ownedStockId;
  private UUID portfolioId;
  private String stockSymbol;
  private int numberOfShares;

  public OwnedStock(UUID ownedStockId, UUID portfolioId, String stockSymbol,
                    int numberOfShares)
  {

    this.ownedStockId   = ownedStockId;
    this.portfolioId    = portfolioId;
    this.stockSymbol    = stockSymbol;
    this.numberOfShares = numberOfShares;
  }

  public UUID getOwnedStockId()
  {
    return ownedStockId;
  }

  public UUID getPortfolioId()
  {
    return portfolioId;
  }

  public String getStockSymbol()
  {
    return stockSymbol;
  }

  public int getNumberOfShares()
  {
    return numberOfShares;
  }
}
