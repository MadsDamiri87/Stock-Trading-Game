package persistence.interfaces;

import entities.Stock;

import java.util.List;
import java.util.Optional;

public interface StockDAO
{
  void create(Stock s);
  void update(Stock s);

  Optional<Stock> getBySymbol(String symbol);

  List<Stock> getAll();

  void delete(String symbol);

}
