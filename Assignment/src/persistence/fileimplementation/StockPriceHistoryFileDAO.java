package persistence.fileimplementation;

import entities.StockPriceHistory;
import persistence.interfaces.StockPriceHistoryDAO;

import java.util.ArrayList;
import java.util.List;

public class StockPriceHistoryFileDAO implements StockPriceHistoryDAO
{
  private final FileUnitOfWork uow;

  public StockPriceHistoryFileDAO(FileUnitOfWork uow)
  {
    this.uow = uow;
  }

  @Override
  public void create(StockPriceHistory history)
  {
    uow.getStockPriceHistories().add(history);
  }


  @Override
  public List<StockPriceHistory> getAll()
  {
    return uow.getStockPriceHistories();
  }

  @Override
  public List<StockPriceHistory> getByStockSymbol(String symbol)
  {
    List<StockPriceHistory> result = new ArrayList<>();

    for (StockPriceHistory h : uow.getStockPriceHistories())
    {
      if (h.getStockSymbolId().equals(symbol))
      {
        result.add(h);
      }
    }

    return result;
  }

}
