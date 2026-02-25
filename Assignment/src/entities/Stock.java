package entities;

import java.math.BigDecimal;

public class Stock
{
  private final String symbol;
  private final String name;
  private BigDecimal currentPrice;
  private String currentState;

  public Stock(String symbol, String name, BigDecimal currentPrice,
               String currentState)
  {

    this.symbol       = symbol;
    this.name         = name;
    this.currentPrice = currentPrice;
    this.currentState = currentState;
  }

  public String getSymbol()
  {
    return symbol;
  }

  public String getName()
  {
    return name;
  }

  public BigDecimal getCurrentPrice()
  {
    return currentPrice;
  }

  public String getCurrentState()
  {
    return currentState;
  }

  public void setCurrentPrice(BigDecimal currentPrice)
  {
    this.currentPrice = currentPrice;
  }

  public void setCurrentState(String currentState)
  {
    this.currentState = currentState;
  }
}
