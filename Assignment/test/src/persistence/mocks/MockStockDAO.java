package persistence.mocks;

import entities.Stock;
import persistence.interfaces.StockDAO;

import java.util.*;

public class MockStockDAO implements StockDAO
{

  private Map<String, Stock> data = new HashMap<>();

  @Override public void create(Stock s)
  {
    data.put(s.getSymbol(), s);
  }

  @Override public void update(Stock s)
  {
    data.put(s.getSymbol(), s);
  }

  @Override public Optional<Stock> getBySymbol(String symbol)
  {
    return Optional.ofNullable(data.get(symbol));
  }

  @Override public List<Stock> getAll()
  {
    return new ArrayList<>(data.values());
  }

  @Override public void delete(String symbol)
  {
    data.remove(symbol);
  }
}
