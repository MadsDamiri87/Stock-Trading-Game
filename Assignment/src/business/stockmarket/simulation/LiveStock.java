package business.stockmarket.simulation;

public class LiveStock
{

  private String stockSymbol;
  private LiveStockState currentState;
  private double currentPrice;

  public LiveStock(String stockSymbol, LiveStockState currentState,
                   double currentPrice)
  {
    this.stockSymbol  = stockSymbol;
    this.currentState = currentState;
    this.currentPrice = currentPrice;
  }

  public LiveStock(String stockSymbol)
  {
    this.stockSymbol  = stockSymbol;
    this.currentState = new SteadyState();
    this.currentPrice = 100.0;
  }

  public void updatePrice()
  {
    double priceChange = currentState.calculatePriceChange(this);

    currentPrice += priceChange;

    if (currentPrice <= 0)
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

  public void setStockSymbol(String stockSymbol)
  {
    this.stockSymbol = stockSymbol;
  }

  public void setCurrentState(LiveStockState currentState)
  {
    this.currentState = currentState;
  }
}
