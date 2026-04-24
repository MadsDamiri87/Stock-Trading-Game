package test.persistence.mocks;

import entities.Portfolio;
import persistence.interfaces.PortfolioDAO;

import java.util.*;

public class MockPortfolioDAO implements PortfolioDAO
{

  private Map<UUID, Portfolio> data = new HashMap<>();

  @Override public Optional<Portfolio> getById(UUID id)
  {
    return Optional.ofNullable(data.get(id));
  }

  @Override public void create(Portfolio portfolio)
  {
    data.put(portfolio.getPortfolioId(), portfolio);
  }

  @Override public void update(Portfolio portfolio)
  {
    data.put(portfolio.getPortfolioId(), portfolio);
  }

  @Override public void delete(UUID id)
  {
    data.remove(id);
  }

  @Override public List<Portfolio> getAll()
  {
    return new ArrayList<>(data.values());
  }
}
