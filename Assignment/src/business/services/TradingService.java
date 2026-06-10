package business.services;

import business.dto.TradeRequestDTO;
import business.services.interfaces.TradingServiceInterface;
import business.strategies.fee.FeeStrategyProvider;
import entities.OwnedStock;
import entities.Portfolio;
import entities.Stock;
import entities.Transaction;
import persistence.interfaces.*;
import shared.logging.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TradingService implements TradingServiceInterface
{

  private final UnitOfWork uow;
  private final PortfolioDAO portfolioDAO;
  private final StockDAO stockDAO;
  private final OwnedStockDAO ownedStockDAO;
  private final TransactionDAO transactionDAO;
  private final FeeStrategyProvider feeStrategyProvider;
  private final Logger logger = Logger.getInstance();

  public TradingService(UnitOfWork uow, PortfolioDAO portfolioDAO, StockDAO stockDAO,
                        OwnedStockDAO ownedStockDAO, TransactionDAO transactionDAO,
                        FeeStrategyProvider feeStrategyProvider)
  {
    this.uow                 = uow;
    this.portfolioDAO        = portfolioDAO;
    this.stockDAO            = stockDAO;
    this.ownedStockDAO       = ownedStockDAO;
    this.transactionDAO      = transactionDAO;
    this.feeStrategyProvider = feeStrategyProvider;
  }

  @Override public void buyStock(TradeRequestDTO request)
  {
    UUID portfolioId = request.portfolioId();
    String stockSymbol = request.stockSymbol();
    int quantity = request.quantity();
    double tradePrice = request.tradePrice();

    logger.log("Info",
               "BuyStock started for portfolioID: " + portfolioId + ", stockSymbol: " + stockSymbol
                   + ", quantity: " + quantity);

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
      BigDecimal totalAmount = pricePerShare.multiply(BigDecimal.valueOf(quantity));

      BigDecimal fee = feeStrategyProvider.getCurrentStrategy()
                                          .calculateFee(pricePerShare, quantity);
      BigDecimal totalCost = totalAmount.add(fee);

      BigDecimal portfolioBalance = portfolio.getCurrentBalance();

      boolean insufficientFunds = portfolioBalance.compareTo(totalCost) < 0;

      if (insufficientFunds)
      {
        logger.log("Error",
                   "Insufficient funds for portfolioId: " + portfolioId + ". Required: " + totalCost
                       + ", available: " + portfolio.getCurrentBalance());
        throw new RuntimeException("Insufficient funds");
      }

      portfolio.setCurrentBalance(portfolio.getCurrentBalance().subtract(totalCost));
      portfolioDAO.update(portfolio);

      OwnedStock ownedStock = ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, stockSymbol)
                                           .orElse(null);

      if (ownedStock == null)
      {
        ownedStock = new OwnedStock(UUID.randomUUID(), portfolioId, stockSymbol, quantity,
                                    tradePrice);

        ownedStockDAO.create(ownedStock);

        logger.log("Info",
                   "Created new OwnedStock for symbol: " + stockSymbol + ", quantity: " + quantity);
      }
      else
      {
        ownedStock.setNumberOfShares(ownedStock.getNumberOfShares() + quantity);
        ownedStock.setTradePrice(tradePrice);
        ownedStockDAO.update(ownedStock);

        logger.log("Info", "Updated OwnedStock for symbol: " + stockSymbol + ", new quantity: "
            + ownedStock.getNumberOfShares());
      }

      Transaction transaction = new Transaction(UUID.randomUUID(), portfolioId, stockSymbol, "BUY",
                                                quantity, pricePerShare, totalAmount, fee,
                                                Instant.now());

      transactionDAO.create(transaction);

      uow.commit();

      logger.log("Info", "BuyStock completed for portfolioId: " + portfolioId + ", stockSymbol: "
          + stockSymbol + ", quantity: " + quantity + ", new balance: "
          + portfolio.getCurrentBalance());

    }
    catch (Exception e)
    {
      uow.rollback();
      logger.log("Error", "BuyStock failed: " + e.getMessage());
      throw e;
    }
  }

  @Override public void sellStock(TradeRequestDTO request)
  {
    UUID portfolioId = request.portfolioId();
    String stockSymbol = request.stockSymbol();
    int quantity = request.quantity();

    logger.log("Info",
               "SellStock started for portfolioID: " + portfolioId + ", stockSymbol: " + stockSymbol
                   + ", quantity: " + quantity);

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

      OwnedStock ownedStock = ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, stockSymbol)
                                           .orElseThrow(() -> new RuntimeException(
                                               "OwnedStock not found for portfolioId: "
                                                   + portfolioId + ", stockSymbol: "
                                                   + stockSymbol));

      if (ownedStock.getNumberOfShares() < quantity)
      {
        logger.log("Error", "Not enough shares to sell. Requested: " + quantity + ", owned: "
            + ownedStock.getNumberOfShares());
        throw new RuntimeException("Not enough shares to sell");
      }

      BigDecimal pricePerShare = stock.getCurrentPrice();
      BigDecimal totalAmount = pricePerShare.multiply(BigDecimal.valueOf(quantity));
      BigDecimal fee = feeStrategyProvider.getCurrentStrategy().calculateFee(pricePerShare, quantity);
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

        logger.log("Info", "Updated OwnedStock after sale for symbol: " + stockSymbol
            + ", remaining shares: " + remainingShares);
      }

      Transaction transaction = new Transaction(UUID.randomUUID(), portfolioId, stockSymbol, "SELL",
                                                quantity, pricePerShare, totalAmount, fee,
                                                Instant.now());
      transactionDAO.create(transaction);

      uow.commit();

      logger.log("Info", "SellStock completed for portfolioId: " + portfolioId + ", stockSymbol: "
          + stockSymbol + ", quantity: " + quantity + ", new balance: "
          + portfolio.getCurrentBalance());

    }
    catch (Exception e)
    {
      uow.rollback();
      logger.log("Error", "SellStock failed: " + e.getMessage());
      throw e;
    }

  }

}
