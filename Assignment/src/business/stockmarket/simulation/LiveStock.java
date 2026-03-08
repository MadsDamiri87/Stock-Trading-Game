package business.stockmarket.simulation;

import shared.configuration.AppConfig;

public class LiveStock
{

  private final String stockSymbol;
  private LiveStockState currentState;
  private double currentPrice;

  public LiveStock(String stockSymbol)
  {
    this.stockSymbol  = stockSymbol;
    this.currentState = new SteadyState();
    this.currentPrice = AppConfig.getInstance().getStartingBalance();
  }

  public LiveStock(String stockSymbol, LiveStockState currentState,
                   double currentPrice)
  {
    this.stockSymbol  = stockSymbol;
    this.currentState = currentState;
    this.currentPrice = currentPrice;
  }

  public void updatePrice()
  {
    double priceChange = currentState.calculatePriceChange(this);

    currentPrice += priceChange;

    if (currentPrice <= 0 && !(currentState instanceof BankruptState) )
    {
      currentPrice = 0;
      setState(new BankruptState());
    }
  }

  public void setState(LiveStockState currentState)
  {
    this.currentState = currentState;
  }

  public String getStockSymbol()
  {
    return stockSymbol;
  }

  public LiveStockState getCurrentState()
  {
    return currentState;
  }

  public double getCurrentPrice()
  {
    return currentPrice;
  }

  public void setCurrentPrice(double currentPrice)
  {
    this.currentPrice = currentPrice;
  }

  public String getCurrentStateName()
  {
    return currentState.getStateName();
  }
}
