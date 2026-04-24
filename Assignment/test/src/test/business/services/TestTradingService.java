package test.business.services;

import business.dto.TradeRequestDTO;
import entities.OwnedStock;
import entities.Portfolio;
import entities.Stock;
import entities.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.persistence.mocks.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TestTradingService
{
  private TradingService tradingService;
  private MockPortfolioDAO portfolioDAO;
  private MockStockDAO stockDAO;
  private MockOwnedStockDAO ownedStockDAO;
  private MockTransactionDAO transactionDAO;
  private MockUnitOfWork uow;

  @BeforeEach void setup()
  {
    uow            = new MockUnitOfWork();
    portfolioDAO   = new MockPortfolioDAO();
    stockDAO       = new MockStockDAO();
    ownedStockDAO  = new MockOwnedStockDAO();
    transactionDAO = new MockTransactionDAO();

    tradingService = new TradingService(uow, portfolioDAO, stockDAO, ownedStockDAO, transactionDAO);
  }

  @Test void buyStock_validInput_shouldUpdateBalance()
  {
    //     ARRANGE
    UUID portfolioId = UUID.randomUUID();

    Portfolio portfolio = new Portfolio(portfolioId, BigDecimal.valueOf(1000));
    portfolioDAO.create(portfolio);

    Stock stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady");

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 4);

    //     ACT
    Portfolio updated = portfolioDAO.getById(portfolioId).get();

    stockDAO.create(stock);
    tradingService.buyStock(request);

    //     ASSERT

    assertEquals(BigDecimal.valueOf(595.0), updated.getCurrentBalance());
  }

  @Test void buyStock_insufficientFunds_shouldThrow()
  {
    //     ARRANGE

    UUID portfolioId = UUID.randomUUID();

    Portfolio portfolio = new Portfolio(portfolioId, BigDecimal.valueOf(100));
    portfolioDAO.create(portfolio);

    Stock stock = new Stock("TSLA", "Tesla", BigDecimal.valueOf(90), "Steady");
    stockDAO.create(stock);

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "TSLA", 2);

    //    ACT + ASSERT

    assertThrows(RuntimeException.class, () -> {
      tradingService.buyStock(request);
    });

  }

  @Test void buyStock_insufficientFunds_shouldRollBack()
  {
    //     ARRANGE

    UUID portfolioId = UUID.randomUUID();

    Portfolio portfolio = new Portfolio(portfolioId, BigDecimal.valueOf(100));
    portfolioDAO.create(portfolio);

    Stock stock = new Stock("TSLA", "Tesla", BigDecimal.valueOf(90), "Steady");
    stockDAO.create(stock);

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "TSLA", 2);

    try
    {
      tradingService.buyStock(request);
    }
    catch (RuntimeException ignored)
    {
      //      expected
    }
    //   ASSERT
    assertTrue(uow.rollback);

  }

  @Test void buyStock_existingStock_shouldUpdateQuantity()
  {
    //    ARRANGE

    UUID portfolioId = UUID.randomUUID();

    Portfolio portfolio = new Portfolio(portfolioId, BigDecimal.valueOf(1000));
    portfolioDAO.create(portfolio);

    Stock stock = new Stock("NKA", "Nokia", BigDecimal.valueOf(100), "Steady");
    stockDAO.create(stock);

    UUID ownedStockId = UUID.randomUUID();

    ownedStockDAO.create(new OwnedStock(ownedStockId, portfolioId, "NKA", 2, 100));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "NKA", 3, 210);

    //    ACT

    tradingService.buyStock(request);
    OwnedStock updated = ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "NKA").get();

    //    ASSERT

    assertEquals(5, updated.getNumberOfShares());

  }

  @Test void buyStock_shouldCreateTransaction()
  {
    // ARRANGE
    UUID portfolioId = UUID.randomUUID();

    Portfolio portfolio = new Portfolio(portfolioId, BigDecimal.valueOf(1000));
    portfolioDAO.create(portfolio);

    Stock stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady");
    stockDAO.create(stock);

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 1, 213);

    // ACT
    tradingService.buyStock(request);

    // ASSERT
    List<Transaction> transactions = transactionDAO.getByPortfolioId(portfolioId, 0, 10);

    assertEquals(1, transactions.size());
  }

  @Test void buyStock_newStock_shouldCreateOwnedStock()
  {
    // ARRANGE
    UUID portfolioId = UUID.randomUUID();

    Portfolio portfolio = new Portfolio(portfolioId, BigDecimal.valueOf(1000));
    portfolioDAO.create(portfolio);

    Stock stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady");
    stockDAO.create(stock);

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 2, 231);

    // ACT
    tradingService.buyStock(request);

    // ASSERT
    OwnedStock owned = ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "AAPL")
                                    .orElseThrow();

    assertEquals(2, owned.getNumberOfShares());
  }

  @Test void buyStock_shouldCommitOnSuccess()
  {
    // ARRANGE
    UUID portfolioId = UUID.randomUUID();

    Portfolio portfolio = new Portfolio(portfolioId, BigDecimal.valueOf(1000));
    portfolioDAO.create(portfolio);

    Stock stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady");
    stockDAO.create(stock);

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 1, 123);

    // ACT
    tradingService.buyStock(request);

    // ASSERT
    assertTrue(uow.commit);
  }

  @Test void buyStock_stockNotFound_shouldThrow()
  {
    // ARRANGE
    UUID portfolioId = UUID.randomUUID();

    Portfolio portfolio = new Portfolio(portfolioId, BigDecimal.valueOf(1000));
    portfolioDAO.create(portfolio);

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 1,213);

    // ACT + ASSERT
    assertThrows(RuntimeException.class, () -> {
      tradingService.buyStock(request);
    });
  }

  @Test void buyStock_insufficientFunds_shouldNotChangeBalance()
  {
    // ARRANGE
    UUID portfolioId = UUID.randomUUID();

    Portfolio portfolio = new Portfolio(portfolioId, BigDecimal.valueOf(100));
    portfolioDAO.create(portfolio);

    Stock stock = new Stock("TSLA", "Tesla", BigDecimal.valueOf(90), "Steady");
    stockDAO.create(stock);

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "TSLA", 2, 213);

    // ACT
    try
    {
      tradingService.buyStock(request);
    }
    catch (RuntimeException ignored)
    {
    }

    // ASSERT
    Portfolio unchanged = portfolioDAO.getById(portfolioId).get();

    assertEquals(BigDecimal.valueOf(100), unchanged.getCurrentBalance());
  }

  @Test void buyStock_negativeQuantity_shouldThrow()
  {
    //    ARRANGE
    UUID portfolioId = UUID.randomUUID();

    Portfolio portfolio = new Portfolio(portfolioId, BigDecimal.valueOf(1000));
    portfolioDAO.create(portfolio);

    Stock stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady");
    stockDAO.create(stock);

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", -1,213);

    //    ACT + ASSERT
    assertThrows(RuntimeException.class, () -> {
      tradingService.buyStock(request);
    });
  }

  @Test void sellStock_validInput_shouldUpdateBalance()
  {
    // ARRANGE
    UUID portfolioId = UUID.randomUUID();

    Portfolio portfolio = new Portfolio(portfolioId, BigDecimal.valueOf(1000));
    portfolioDAO.create(portfolio);

    Stock stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady");
    stockDAO.create(stock);

    ownedStockDAO.create(new OwnedStock(UUID.randomUUID(), portfolioId, "AAPL", 5, 200));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 2,215);

    // ACT
    tradingService.sellStock(request);

    // ASSERT
    Portfolio updated = portfolioDAO.getById(portfolioId).get();
    assertEquals(BigDecimal.valueOf(1195.0), updated.getCurrentBalance());
  }

  @Test void sellStock_shouldReduceOwnedStockQuantity()
  {

    //    ARRANGE
    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));

    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    ownedStockDAO.create(new OwnedStock(UUID.randomUUID(), portfolioId, "AAPL", 5, 200));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 3, 213);

    //    ACT
    tradingService.sellStock(request);

    //    ASSERT
    OwnedStock updated = ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "AAPL")
                                      .orElseThrow();

    assertEquals(2, updated.getNumberOfShares());
  }

  @Test void sellStock_notEnoughShares_shouldThrow()
  {
    //    ARRANGE
    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));

    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    ownedStockDAO.create(new OwnedStock(UUID.randomUUID(), portfolioId, "AAPL", 2, 200));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 5,120);

    //    ACT + ASSERT
    assertThrows(RuntimeException.class, () -> {
      tradingService.sellStock(request);
    });
  }

  @Test void sellStock_shouldCreateTransaction()
  {
    //    ARRANGE
    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));

    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    ownedStockDAO.create(new OwnedStock(UUID.randomUUID(), portfolioId, "AAPL", 5, 300));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 1, 210);

    //    ACT
    tradingService.sellStock(request);

    List<Transaction> transactions = transactionDAO.getByPortfolioId(portfolioId, 0, 10);

    //    ASSERT
    assertEquals(1, transactions.size());
  }

  @Test void sellStock_shouldCommitOnSuccess()
  {
    //    ARRANGE
    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));

    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    ownedStockDAO.create(new OwnedStock(UUID.randomUUID(), portfolioId, "AAPL", 5, 12));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 1, 120);
    //    ACT
    tradingService.sellStock(request);

    //    ASSERT
    assertTrue(uow.commit);
  }

  @Test void sellStock_stockNotOwned_shouldThrow()
  {

    //    ARRANGE
    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));

    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 1, 110);

    //   ACT + ASSERT
    assertThrows(RuntimeException.class, () -> {
      tradingService.sellStock(request);
    });
  }

  @Test void sellStock_invalidQuantity_shouldThrow()
  {
    //    ARRANGE
    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));

    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    ownedStockDAO.create(new OwnedStock(UUID.randomUUID(), portfolioId, "AAPL", 5, 21));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 0,132);

    //    ACT + ASSERT
    assertThrows(RuntimeException.class, () -> {
      tradingService.sellStock(request);
    });
  }

  @Test void sellStock_shouldRemoveOwnedStock_whenQuantityZero()
  {
    //    ARRANGE
    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));

    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    ownedStockDAO.create(new OwnedStock(UUID.randomUUID(), portfolioId, "AAPL", 2, 230));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 2,120);

    //    ACT
    tradingService.sellStock(request);

    //    ASSERT
    Optional<OwnedStock> result = ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "AAPL");
    assertTrue(result.isEmpty());
  }

  @Test void sellStock_shouldRollbackOnFailure()
  {
    //    ARRANGE

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));

    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    ownedStockDAO.create(new OwnedStock(UUID.randomUUID(), portfolioId, "AAPL", 1, 32));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 5,130);

    //    ACT
    try
    {
      tradingService.sellStock(request);
    }
    catch (RuntimeException ignored)
    {
    }

    //    ASSERT
    assertTrue(uow.rollback);
  }

}
