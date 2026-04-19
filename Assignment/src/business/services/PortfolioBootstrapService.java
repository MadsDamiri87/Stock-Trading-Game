package business.services;

import entities.Portfolio;
import persistence.interfaces.PortfolioDAO;
import persistence.interfaces.UnitOfWork;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PortfolioBootstrapService
{
  private final UnitOfWork unitOfWork;
  private final PortfolioDAO portfolioDAO;

  public PortfolioBootstrapService(UnitOfWork unitOfWork, PortfolioDAO portfolioDAO)
  {
    this.unitOfWork   = unitOfWork;
    this.portfolioDAO = portfolioDAO;
  }

  public UUID getOrCreatePortfolioId()
  {
    List<Portfolio> portfolios = portfolioDAO.getAll();

    if (!portfolios.isEmpty())
    {
      return portfolios.getFirst().getPortfolioId();
    }
    try
    {
      unitOfWork.beginTransaction();

      Portfolio portfolio = new Portfolio(UUID.randomUUID(), BigDecimal.valueOf(10000));
      portfolioDAO.create(portfolio);

      unitOfWork.commit();
      return portfolio.getPortfolioId();
    }
    catch (Exception e)
    {
      unitOfWork.rollback();
      throw e;
    }
  }
}
