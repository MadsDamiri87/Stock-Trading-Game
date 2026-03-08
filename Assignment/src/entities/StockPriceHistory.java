package entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class StockPriceHistory
{
  private final UUID stockPriceHistId;
  private final String stockSymbolId;
  private final BigDecimal price;
  private final Instant timestamp;

  //Hvis databasen skal stå for UUID, skal der være 2 constructors og constructoren skal overloades.

  public StockPriceHistory(UUID stockPriceHistId, String stockSymbolId,
                           BigDecimal price, Instant timestamp)
  {

    this.stockPriceHistId = stockPriceHistId;
    this.stockSymbolId    = stockSymbolId;
    this.price            = price;
    this.timestamp        = timestamp;

  }

  public UUID getStockPriceHistId()
  {
    return stockPriceHistId;
  }

  public String getStockSymbolId()
  {
    return stockSymbolId;
  }

  public BigDecimal getPrice()
  {
    return price;
  }

  public Instant getTimestamp()
  {
    return timestamp;
  }
}
