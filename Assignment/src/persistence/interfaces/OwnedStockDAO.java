package persistence.interfaces;

import entities.OwnedStock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OwnedStockDAO
{

  Optional<OwnedStock> getById(UUID ownedStockId);

  void create(OwnedStock ownedStock);
  void update(OwnedStock update);
  void delete(UUID uuid);

  List<OwnedStock> getAll();
}
