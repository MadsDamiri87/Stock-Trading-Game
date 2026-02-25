package persistence.fileimplementation;

import entities.Stock;
import persistence.interfaces.StockDAO;

import java.util.List;
import java.util.Optional;

public class StockFileDAO implements StockDAO
{
  private final FileUnitOfWork uow;

  public StockFileDAO(FileUnitOfWork uow)
  {
    this.uow = uow;
  }

  @Override public void create(Stock s)
  {
    if (getBySymbol(s.getSymbol()).isPresent())
    {
      throw new RuntimeException(
          "Der findes allerede en aktie med symbolet: " + s.getSymbol());
    }
    uow.getStocks().add(s);
  }

  @Override public void update(Stock updated)
  {
    List<Stock> list = uow.getStocks();
    for (int i = 0; i < list.size(); i++)
    {
      Stock existing = list.get(i);

      if (existing.getSymbol().equalsIgnoreCase(updated.getSymbol()))
      {
        list.set(i, updated);
        return;
      }
    }
    throw new RuntimeException(
        "Stock med symbolet: " + updated.getSymbol() + " blev ikke fundet");
  }

  @Override public Optional<Stock> getBySymbol(String symbol)
  {
    for (Stock s : uow.getStocks())
    {
      if (s.getSymbol().equalsIgnoreCase(symbol))
      {
        return Optional.of(s);
      }
    }
    return Optional.empty();
  }

  @Override public List<Stock> getAll()
  {
    return uow.getStocks();
  }

  @Override public void delete(String symbol)
  {
    boolean removed = uow.getStocks()
                         .removeIf(s -> s.getSymbol().equalsIgnoreCase(symbol));

    if (!removed)
    {
      throw new RuntimeException(
          "Stock med symbolet: '" + symbol + "' blev ikke fundet. ");
    }
  }
}
