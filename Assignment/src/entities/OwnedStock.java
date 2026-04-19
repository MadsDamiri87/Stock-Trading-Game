package entities;

import java.util.UUID;

public class OwnedStock
{

  private final UUID ownedStockId;
  private UUID portfolioId;
  private String stockSymbol;
  private int numberOfShares;
  private double tradePrice;

  public OwnedStock(UUID ownedStockId, UUID portfolioId, String stockSymbol, int numberOfShares, double tradePrice)
  {

    this.ownedStockId   = ownedStockId;
    this.portfolioId    = portfolioId;
    this.stockSymbol    = stockSymbol;
    this.numberOfShares = numberOfShares;
    this.tradePrice     = tradePrice;
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

  public double getTradePrice()
  {
    return tradePrice;
  }

  public void setPortfolioId(UUID portfolioId)
  {
    this.portfolioId = portfolioId;
  }

  public void setStockSymbol(String stockSymbol)
  {
    this.stockSymbol = stockSymbol;
  }

  public void setNumberOfShares(int numberOfShares)
  {
    this.numberOfShares = numberOfShares;
  }

  public void setTradePrice(double boughtPrice)
  {
    this.tradePrice = boughtPrice;
  }


}
