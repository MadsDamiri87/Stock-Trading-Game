package business.services;

import business.dto.*;
import business.services.mapping.OwnedStockMapper;
import business.services.mapping.PortfolioMapper;
import business.services.mapping.StockMapper;
import business.services.mapping.TransactionMapper;
import entities.Portfolio;
import persistence.interfaces.OwnedStockDAO;
import persistence.interfaces.PortfolioDAO;
import persistence.interfaces.StockDAO;
import persistence.interfaces.TransactionDAO;
import shared.logging.Logger;

import java.util.List;
import java.util.UUID;

public class PortfolioService
{
  private final PortfolioDAO portfolioDAO;
  private final OwnedStockDAO ownedStockDAO;
  private final TransactionDAO transactionDAO;
  private final StockDAO stockDAO;
  private final Logger logger = Logger.getInstance();

  public PortfolioService(PortfolioDAO portfolioDAO, OwnedStockDAO ownedStockDAO,
                          TransactionDAO transactionDAO, StockDAO stockDAO)
  {
    this.portfolioDAO   = portfolioDAO;
    this.ownedStockDAO  = ownedStockDAO;
    this.transactionDAO = transactionDAO;
    this.stockDAO       = stockDAO;
  }

  public PortfolioDTO getPortfolio(UUID portfolioId)
  {
    logger.log("Info", "Fetching portfolio: " + portfolioId);
    Portfolio portfolio = portfolioDAO.getById(portfolioId).orElseThrow(
        () -> new RuntimeException("Portfolio not found: " + portfolioId));

    List<OwnedStockDTO> ownedStockDTOs = ownedStockDAO.getByPortfolioId(portfolioId).stream()
                                                      .map(OwnedStockMapper::toOwnedStockDTO)
                                                      .toList();
    return PortfolioMapper.toPortfolioDTO(portfolio, ownedStockDTOs);
  }

  public List<TransactionDTO> getTransactionHistory(UUID portfolioId, int page, int size)
  {
    logger.log("Info", "Fetching TransactionHistory: " + portfolioId);

    if (page < 0 || size <= 0)
    {
      throw new IllegalArgumentException("Page must be equal to og greater than 0, and size must be greater than 0");
    }

    int offset = page * size;

    return transactionDAO.getByPortfolioId(portfolioId, offset, size).stream()
                         .map(TransactionMapper::toTransactionDTO).toList();
  }

  public List<StockDTO> getAvailableStocks()
  {
    logger.log("Info", "Fetching AvailableStocks");

    return stockDAO.getAll().stream().map(StockMapper::toStockDTO).toList();
  }

}
