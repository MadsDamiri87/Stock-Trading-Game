package business.services;

import entities.Stock;
import persistence.interfaces.StockDAO;

import java.math.BigDecimal;
import java.util.Optional;

public class StockSetupService
{
  private final StockDAO stockDAO;

  public StockSetupService(StockDAO stockDAO)
  {
    this.stockDAO = stockDAO;
  }

  public Stock getOrCreateStock(String symbol, String name, BigDecimal price,
                                String state)
  {
    Optional<Stock> optionalStock = stockDAO.getBySymbol(symbol);

    if (optionalStock.isPresent())
    {
      return optionalStock.get();
    }
    else
    {
      Stock stock = new Stock(symbol, name, price, state);
      stockDAO.create(stock);
      return stock;
    }
  }
}