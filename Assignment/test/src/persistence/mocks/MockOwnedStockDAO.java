package persistence.mocks;

import entities.OwnedStock;
import persistence.interfaces.OwnedStockDAO;

import java.util.*;

public class MockOwnedStockDAO implements OwnedStockDAO
{
  private final Map<UUID, OwnedStock> data = new HashMap<>();

  @Override public Optional<OwnedStock> getById(UUID ownedStockId)
  {
    return Optional.ofNullable(data.get(ownedStockId));
  }

  @Override public Optional<OwnedStock> getByPortfolioIdAndStockSymbol(UUID portfolioId,
                                                                       String stockSymbol)
  {
    return data.values().stream()
        .filter(s->s.getPortfolioId().equals(portfolioId))
        .filter(s->s.getStockSymbol().equals(stockSymbol))
        .findFirst();
  }

  @Override public List<OwnedStock> getByPortfolioId(UUID portfolioId)
  {
    return data.values().stream()
        .filter(s->s.getPortfolioId().equals(portfolioId))
        .toList();
  }

  @Override public void create(OwnedStock ownedStock)
  {
    data.put(ownedStock.getOwnedStockId(), ownedStock);
  }

  @Override public void update(OwnedStock update)
  {
    data.put(update.getOwnedStockId(), update);
  }

  @Override public void delete(UUID uuid)
  {
    data.remove(uuid);
  }

  @Override public List<OwnedStock> getAll()
  {
    return new ArrayList<>(data.values());
  }
}
