package business.stockmarket.simulation;

import entities.Stock;

import java.math.BigDecimal;

public class LiveStock
{

  private String stockSymbol;
  private BigDecimal currentState;
  private double currentPrice;

  public LiveStock(String stockSymbol, BigDecimal currentState, double currentPrice)
  {
    this.stockSymbol  = stockSymbol;
    this.currentState        = currentState;
    this.currentPrice = currentPrice;
  }

  public LiveStock(String stockSymbol)
  {
    this.stockSymbol = stockSymbol;
  }



  public void updatePrice(){
    double priceChange = currentState.calculatePriceChange(this);

    currentPrice += priceChange;
  }
}
