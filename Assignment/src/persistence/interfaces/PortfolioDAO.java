package persistence.interfaces;

import entities.Portfolio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioDAO
{

  Optional<Portfolio> getById(UUID id);

  void create(Portfolio portfolio);
  void update(Portfolio portfolio);
  void delete(UUID id);

  List<Portfolio> getAll();
}
