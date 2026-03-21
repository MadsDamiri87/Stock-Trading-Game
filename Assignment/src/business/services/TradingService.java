package business.services;

import business.dto.TradeResultDTO;
import entities.OwnedStock;
import entities.Portfolio;
import entities.Stock;
import entities.Transaction;
import persistence.interfaces.*;
import shared.configuration.AppConfig;
import shared.logging.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TradingService
{

  private final UnitOfWork uow;
  private final PortfolioDAO portfolioDAO;
  private final StockDAO stockDAO;
  private final OwnedStockDAO ownedStockDAO;
  private final TransactionDAO transactionDAO;
  private final Logger logger = Logger.getInstance();

  public TradingService(UnitOfWork uow, PortfolioDAO portfolioDAO,
                        StockDAO stockDAO, OwnedStockDAO ownedStockDAO,
                        TransactionDAO transactionDAO)
  {
    this.uow            = uow;
    this.portfolioDAO   = portfolioDAO;
    this.stockDAO       = stockDAO;
    this.ownedStockDAO  = ownedStockDAO;
    this.transactionDAO = transactionDAO;
  }

  public TradeResultDTO buyStock(UUID portfolioId, String stockSymbol,
                                 int quantity)
  {
    logger.log("Info", "BuyStock started for portfolioID: " + portfolioId
        + ", stockSymbol: " + stockSymbol + ", quantity: " + quantity);

    if (quantity <= 0)
    {

      logger.log("Error", "Invalid quantity for stock purchase: " + quantity);
      throw new IllegalArgumentException("Quantity must be greater then 0");
    }
    try
    {
      uow.beginTransaction();

      Portfolio portfolio = portfolioDAO.getById(portfolioId).orElseThrow(
          () -> new RuntimeException("Portfolio not found: " + portfolioId));

      Stock stock = stockDAO.getBySymbol(stockSymbol).orElseThrow(
          () -> new RuntimeException("Stock not found: " + stockSymbol));

      if ("Bankrupt".equalsIgnoreCase(stock.getCurrentState()))
      {
        logger.log("Error", "Cannot buy a bankrupt stock: " + stockSymbol);
        throw new RuntimeException("Cannot buy a bankrupt stock");
      }

      BigDecimal pricePerShare = stock.getCurrentPrice();
      BigDecimal totalAmount = pricePerShare.multiply(
          BigDecimal.valueOf(quantity));

      BigDecimal feeRate = AppConfig.getInstance().getTransactionFee();
      BigDecimal fee = totalAmount.multiply(feeRate);
      BigDecimal totalCost = totalAmount.add(fee);

      if (portfolio.getCurrentBalance().compareTo(totalCost) < 0)
      {
        logger.log("Error", "Insufficient funds for portfolioId: " + portfolioId
            + ". Required: " + totalCost + ", available: "
            + portfolio.getCurrentBalance());
        throw new RuntimeException("Insufficient funds");
      }

      portfolio.setCurrentBalance(
          portfolio.getCurrentBalance().subtract(totalCost));
      portfolioDAO.update(portfolio);

      OwnedStock ownedStock = ownedStockDAO.getByPortfolioIdAndStockSymbol(
          portfolioId, stockSymbol).orElse(null);

      if (ownedStock == null)
      {
        ownedStock = new OwnedStock(UUID.randomUUID(), portfolioId, stockSymbol,
                                    quantity);

        ownedStockDAO.create(ownedStock);

        logger.log("Info", "Created new OwnedStock for symbol: " + stockSymbol
            + ", quantity: " + quantity);
      }
      else
      {
        ownedStock.setNumberOfShares(ownedStock.getNumberOfShares() + quantity);
        ownedStockDAO.update(ownedStock);

        logger.log("Info", "Updated OwnedStock for symbol: " + stockSymbol
            + ", new quantity: " + ownedStock.getNumberOfShares());
      }

      Transaction transaction = new Transaction(UUID.randomUUID(), portfolioId,
                                                stockSymbol, "BUY", quantity,
                                                pricePerShare, totalAmount, fee,
                                                Instant.now());

      transactionDAO.create(transaction);

      uow.commit();

      logger.log("Info", "BuyStock completed for portfolioId: " + portfolioId
          + ", stockSymbol: " + stockSymbol + ", quantity: " + quantity
          + ", new balance: " + portfolio.getCurrentBalance());

      return new TradeResultDTO(stockSymbol, "BUY", quantity, pricePerShare,
                                fee, totalAmount,
                                portfolio.getCurrentBalance());

    }
    catch (Exception e)
    {
      uow.rollback();
      logger.log("Error", "BuyStock failed: " + e.getMessage());
      throw e;
    }
  }

  public TradeResultDTO sellStock(UUID portfolioId, String stockSymbol,
                                  int quantity)
  {
    logger.log("Info", "SellStock started for portfolioID: " + portfolioId
        + ", stockSymbol: " + stockSymbol + ", quantity: " + quantity);

    if (quantity <= 0)
    {
      logger.log("Error", "Invalid quantity for sellStock: " + quantity);
      throw new IllegalArgumentException("Quantity must be greater than 0");
    }

    try
    {
      uow.beginTransaction();

      Portfolio portfolio = portfolioDAO.getById(portfolioId).orElseThrow(
          () -> new RuntimeException("Portfolio not found: " + portfolioId));
      Stock stock = stockDAO.getBySymbol(stockSymbol).orElseThrow(
          () -> new RuntimeException("Stock not found: " + stockSymbol));

      OwnedStock ownedStock = ownedStockDAO.getByPortfolioIdAndStockSymbol(
          portfolioId, stockSymbol).orElseThrow(() -> new RuntimeException(
          "OwnedStock not found for portfolioId: " + portfolioId
              + ", stockSymbol: " + stockSymbol));

      if (ownedStock.getNumberOfShares() < quantity)
      {
        logger.log("Error", "Not enough shares to sell. Requested: " + quantity
            + ", owned: " + ownedStock.getNumberOfShares());
        throw new RuntimeException("Not enough shares to sell");
      }

      BigDecimal pricePerShare = stock.getCurrentPrice();
      BigDecimal totalAmount = pricePerShare.multiply(
          BigDecimal.valueOf(quantity));
      BigDecimal fee = totalAmount.multiply(BigDecimal.valueOf(0.015));
      BigDecimal payout = totalAmount.subtract(fee);

      portfolio.setCurrentBalance(portfolio.getCurrentBalance().add(payout));
      portfolioDAO.update(portfolio);

      int remainingShares = ownedStock.getNumberOfShares() - quantity;

      if (remainingShares == 0)
      {
        ownedStockDAO.delete(ownedStock.getOwnedStockId());
        logger.log("Info", "Deleted OwnedStock for symbol: " + stockSymbol);
      }

      else
      {
        ownedStock.setNumberOfShares(remainingShares);
        ownedStockDAO.update(ownedStock);

        logger.log("Info",
                   "Updated OwnedStock after sale for symbol: " + stockSymbol
                       + ", remaining shares: " + remainingShares);
      }

      Transaction transaction = new Transaction(UUID.randomUUID(), portfolioId,
                                                stockSymbol, "SELL", quantity,
                                                pricePerShare, totalAmount, fee,
                                                Instant.now());
      transactionDAO.create(transaction);

      uow.commit();

      logger.log("Info", "SellStock completed for portfolioId: " + portfolioId
          + ", stockSymbol: " + stockSymbol + ", quantity: " + quantity
          + ", new balance: " + portfolio.getCurrentBalance());

      return new TradeResultDTO(stockSymbol, "SELL", quantity, pricePerShare,
                                fee, payout, portfolio.getCurrentBalance());

    }
    catch (Exception e)
    {
      uow.rollback();
      logger.log("Error", "SellStock failed: " + e.getMessage());
      throw e;
    }

  }

}
