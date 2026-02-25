package persistence.interfaces;

import entities.OwnedStock;
import entities.Portfolio;
import entities.Stock;

import java.util.List;

public interface UnitOfWork
{

  void beginTransaction();
  void commit();
  void rollback();

}
