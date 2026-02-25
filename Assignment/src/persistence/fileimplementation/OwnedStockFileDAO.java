package persistence.fileimplementation;

import entities.OwnedStock;
import persistence.interfaces.OwnedStockDAO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OwnedStockFileDAO implements OwnedStockDAO
{
  private final FileUnitOfWork uow;

  public OwnedStockFileDAO(FileUnitOfWork uow)
  {
    this.uow = uow;
  }

  @Override public void create(OwnedStock ownedStock)
  {
    if (getById(ownedStock.getOwnedStockId()).isPresent())
    {
      throw new RuntimeException(
          "Ownedstock med id: " + ownedStock.getOwnedStockId()
              + " findes allerede");
    }
    uow.getOwnedStocks().add(ownedStock);
  }

  @Override public void update(OwnedStock update)
  {
    List<OwnedStock> list = uow.getOwnedStocks();

    for (int i = 0; i < list.size(); i++)
    {
      OwnedStock existing = list.get(i);

      if (existing.getOwnedStockId().equals(update.getOwnedStockId()))
      {
        list.set(i, update);
        return;
      }
    }
    throw new RuntimeException(
        "Ownedstock med id: " + update.getOwnedStockId() + " blev ikke fundet");
  }

  @Override public void delete(UUID uuid)
  {
    boolean removed = uow.getOwnedStocks()
                         .removeIf(id -> id.getOwnedStockId().equals(uuid));
    if (!removed)
    {
      throw new RuntimeException(
          "Ownedstock med id: '" + uuid + "' blev ikke fundet");
    }
  }

  @Override public Optional<OwnedStock> getById(UUID ownedStockId)
  {
    for (OwnedStock o : uow.getOwnedStocks())
    {
      if (o.getOwnedStockId().equals(ownedStockId))
      {
        return Optional.of(o);
      }
    }
    return Optional.empty();
  }

  @Override public List<OwnedStock> getAll()
  {
    return uow.getOwnedStocks();
  }

}
