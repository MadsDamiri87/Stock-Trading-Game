package persistence.fileimplementation;

import entities.Portfolio;
import persistence.interfaces.PortfolioDAO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PortfolioFileDAO implements PortfolioDAO
{
  private final FileUnitOfWork uow;

  public PortfolioFileDAO(FileUnitOfWork uow)
  {
    this.uow = uow;
  }

  @Override public void create(Portfolio portfolio)
  {
    if (getById(portfolio.getPortfolioId()).isPresent())
    {
      throw new RuntimeException(
          "Portfolio med id: " + portfolio.getPortfolioId()
              + " findes allerede");
    }
    uow.getPortfolios().add(portfolio);
  }

  @Override public void update(Portfolio updated)
  {
    List<Portfolio> list = uow.getPortfolios();

    for (int i = 0; i < list.size(); i++)
    {
      Portfolio existing = list.get(i);

      if (existing.getPortfolioId().equals(updated.getPortfolioId()))
      {
        list.set(i, updated);
        return;
      }
    }
    throw new RuntimeException(
        "Portfolio med id: " + updated.getPortfolioId() + " blev ikke fundet");
  }

  @Override public void delete(UUID id)
  {
    boolean removed = uow.getPortfolios()
                         .removeIf(p -> p.getPortfolioId().equals(id));
    if (!removed)
    {
      throw new RuntimeException(
          "Portfolio med id: '" + id + "' blev ikke fundet");
    }
  }

  @Override public Optional<Portfolio> getById(UUID id)
  {
    for (Portfolio p : uow.getPortfolios())
    {
      if (p.getPortfolioId().equals(id))
      {
        return Optional.of(p);
      }
    }
    return Optional.empty();
  }

  @Override public List<Portfolio> getAll()
  {
    return uow.getPortfolios();
  }
}
