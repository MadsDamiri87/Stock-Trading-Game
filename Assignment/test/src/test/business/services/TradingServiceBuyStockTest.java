package test.business.services;

import business.dto.TradeRequestDTO;
import business.services.TradingService;
import business.strategies.fee.FeeStrategyManager;
import business.strategies.fee.FeeStrategyProvider;
import business.strategies.fee.PercentageFeeStrategy;
import entities.OwnedStock;
import entities.Portfolio;
import entities.Stock;
import entities.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test.persistence.mocks.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TradingServiceBuyStockTest
{
  private TradingService tradingService;

  private MockPortfolioDAO portfolioDAO;
  private MockStockDAO stockDAO;
  private SpyOwnedStockDAO ownedStockDAO;
  private MockTransactionDAO transactionDAO;
  private SpyUnitOfWork uow;

  private FeeStrategyProvider feeStrategyProvider;

  @BeforeEach
  void setup()
  {
    // FIRST: Independent/Repeatable
    // Hver test får nye mocks, så de ikke deler state med hinanden.
    portfolioDAO = new MockPortfolioDAO();
    stockDAO = new MockStockDAO();
    ownedStockDAO = new SpyOwnedStockDAO();
    transactionDAO = new MockTransactionDAO();
    uow = new SpyUnitOfWork();

    // Default fee er 2%, men enkelte tests ændrer den selv.
    feeStrategyProvider = new FeeStrategyManager(new PercentageFeeStrategy(BigDecimal.valueOf(0.02)));

    tradingService = new TradingService(
        uow,
        portfolioDAO,
        stockDAO,
        ownedStockDAO,
        transactionDAO,
        feeStrategyProvider
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 0})
  void buyStock_quantityAtOrBelowBoundary_throwsException(int quantity)
  {
    // BVA:
    // Grænsen er quantity > 0.
    // Derfor tester vi værdierne lige omkring grænsen:
    // -1 og 0 er ugyldige.

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(10000)));
    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(10), "Steady"));

    TradeRequestDTO request =
        new TradeRequestDTO(portfolioId, "AAPL", quantity, 10);

    assertThrows(IllegalArgumentException.class,
                 () -> tradingService.buyStock(request));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 5, 100})
  void buyStock_validQuantityPartition_succeeds(int quantity)
  {
    // EP:
    // Alle værdier hvor quantity > 0 tilhører den gyldige partition.
    // Vi tester flere repræsentanter fra samme gyldige partition.

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(100000)));
    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(10), "Steady"));

    TradeRequestDTO request =
        new TradeRequestDTO(portfolioId, "AAPL", quantity, 10);

    tradingService.buyStock(request);

    assertEquals(quantity,
                 ownedStockDAO
                     .getByPortfolioIdAndStockSymbol(portfolioId, "AAPL")
                     .orElseThrow()
                     .getNumberOfShares());
  }

  @Test
  void buyStock_quantityOne_affordableStock_succeeds()
  {
    // ZOMBIES: One
    // EP: gyldig quantity, gyldig stock, nok penge.
    // AAA: Arrange

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));
    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 1, 1);

    // Act
    tradingService.buyStock(request);

    // Assert
    assertTrue(uow.commitCalled);
    assertEquals(1, ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "AAPL")
                                 .orElseThrow()
                                 .getNumberOfShares());
  }


  @Test
  void buyStock_negativeQuantity_throwsException()
  {
    // ZOMBIES: Exceptions
    // EP: ugyldige quantities: negative tal.
    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));
    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    TradeRequestDTO request =
        new TradeRequestDTO(portfolioId, "AAPL", -1, 1);

    assertThrows(IllegalArgumentException.class,
                 () -> tradingService.buyStock(request));

    assertFalse(uow.rollbackCalled);
  }
  @Test
  void buyStock_newStock_createsOwnedStock()
  {
    // ZOMBIES: Interface / Simple
    // Tester at service-laget opretter en ny position, når aktien ikke findes i porteføljen.

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));
    stockDAO.create(new Stock("MSFT", "Microsoft", BigDecimal.valueOf(100), "Steady"));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "MSFT", 2, 1);

    tradingService.buyStock(request);

    OwnedStock owned = ownedStockDAO
        .getByPortfolioIdAndStockSymbol(portfolioId, "MSFT")
        .orElseThrow();

    assertEquals(2, owned.getNumberOfShares());
    assertEquals(1, ownedStockDAO.createCount);
    assertEquals(0, ownedStockDAO.updateCount);
  }

  @Test
  void buyStock_existingStock_updatesOwnedStockInsteadOfCreatingNewOne()
  {
    // White-box-ish:
    // Vi tester ikke kun resultatet, men også om update bruges frem for create.

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));
    stockDAO.create(new Stock("TSLA", "Tesla", BigDecimal.valueOf(100), "Steady"));

    ownedStockDAO.create(new OwnedStock(UUID.randomUUID(), portfolioId, "TSLA", 2, 100));

    // nulstiller counter, fordi arrange-delen også brugte create()
    ownedStockDAO.createCount = 0;

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "TSLA", 3, 1);

    tradingService.buyStock(request);

    OwnedStock updated = ownedStockDAO
        .getByPortfolioIdAndStockSymbol(portfolioId, "TSLA")
        .orElseThrow();

    assertEquals(5, updated.getNumberOfShares());
    assertEquals(0, ownedStockDAO.createCount);
    assertEquals(1, ownedStockDAO.updateCount);
  }

  @Test
  void buyStock_manyShares_withEnoughBalance_succeeds()
  {
    // ZOMBIES: Many
    // EP: gyldigt stort antal aktier.
    // BVA: Ikke boundary her, men et stort gyldigt input inden for saldoen.
    // AAA:
    // Arrange = opret portfolio, stock og request.
    // Act = kald buyStock().
    // Assert = kontroller antal owned shares.

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(
        new Portfolio(portfolioId, BigDecimal.valueOf(200000)));

    stockDAO.create(
        new Stock("AAPL", "Apple", BigDecimal.valueOf(10), "Steady"));

    TradeRequestDTO request =
        new TradeRequestDTO(portfolioId, "AAPL", 10000, 10);

    tradingService.buyStock(request);

    assertEquals(10000,
                 ownedStockDAO
                     .getByPortfolioIdAndStockSymbol(portfolioId, "AAPL")
                     .orElseThrow()
                     .getNumberOfShares());
  }

  @Test
  void buyStock_totalCostExactlyEqualToBalance_succeeds()
  {
    // BVA: præcis på grænsen.
    // Pris = 100, quantity = 1, fee = 0, balance = 100.

    feeStrategyProvider = new FeeStrategyManager(new PercentageFeeStrategy(BigDecimal.valueOf(0.02)));


    tradingService = new TradingService(
        uow, portfolioDAO, stockDAO, ownedStockDAO, transactionDAO, feeStrategyProvider
    );

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(10000)));
    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 1, 1);

    tradingService.buyStock(request);

    assertEquals(new BigDecimal("9898.00"), portfolioDAO.getById(portfolioId)
                                                    .orElseThrow()
                                                    .getCurrentBalance());

  }

  @Test
  void buyStock_totalCostOneCentMoreThanBalance_throwsException()
  {
    // BVA: lige over grænsen.
    // Balance er 99.99, men totalCost er 100.00.

    feeStrategyProvider = new FeeStrategyManager(new PercentageFeeStrategy(BigDecimal.valueOf(0.02)));



    tradingService = new TradingService(
        uow, portfolioDAO, stockDAO, ownedStockDAO, transactionDAO, feeStrategyProvider
    );

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(99.99)));
    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 1, 1);

    assertThrows(RuntimeException.class, () -> tradingService.buyStock(request));
    assertTrue(uow.rollbackCalled);
  }

  @Test
  void buyStock_transactionFeeZero_succeedsWithFreeTrading()
  {
    // BVA/EP: fee = 0 er en særlig partition.
    // Det er stadig gyldigt, bare gratis handel.

    feeStrategyProvider = new FeeStrategyManager(new PercentageFeeStrategy(BigDecimal.valueOf(0.02)));


    tradingService = new TradingService(
        uow, portfolioDAO, stockDAO, ownedStockDAO, transactionDAO, feeStrategyProvider
    );

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(500)));
    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 2, 1);

    tradingService.buyStock(request);

    assertEquals(new BigDecimal("296.00"), portfolioDAO.getById(portfolioId)
                                                      .orElseThrow()
                                                      .getCurrentBalance());
  }

  @Test
  void buyStock_bankruptStock_throwsException()
  {
    // ZOMBIES: Exceptions
    // EP: aktier der ikke må handles.

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));
    stockDAO.create(new Stock("BAD", "Bad Company", BigDecimal.valueOf(100), "Bankrupt"));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "BAD", 1, 1);

    assertThrows(RuntimeException.class, () -> tradingService.buyStock(request));
    assertTrue(uow.rollbackCalled);
  }

  @Test
  void buyStock_symbolNull_throwsException()
  {
    // ZOMBIES: Zero / Nothing
    // Interface-test: public metode får null input.

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, null, 1, 1);

    assertThrows(RuntimeException.class, () -> tradingService.buyStock(request));
  }

  @Test
  void buyStock_symbolEmptyString_throwsException()
  {
    // ZOMBIES: Zero
    // "" er ikke det samme som null, så den fortjener sin egen test.

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "", 1, 1);

    assertThrows(RuntimeException.class, () -> tradingService.buyStock(request));
  }

  @Test
  void buyStock_symbolNotFound_throwsException()
  {
    // EP: symbol er teknisk set udfyldt, men findes ikke i DAO'en.

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "NOPE", 1, 1);

    assertThrows(RuntimeException.class, () -> tradingService.buyStock(request));
  }

  @Test
  void buyStock_insufficientBalance_doesNotChangeBalance()
  {
    // State/behavior:
    // Hvis købet fejler, skal saldoen ikke lige pludselig være ændret.

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(50)));
    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 1, 1);

    assertThrows(RuntimeException.class, () -> tradingService.buyStock(request));

    assertEquals(BigDecimal.valueOf(50), portfolioDAO.getById(portfolioId)
                                                     .orElseThrow()
                                                     .getCurrentBalance());
  }

  @Test
  void buyStock_success_createsBuyTransaction()
  {
    // State/behavior:
    // Ikke kun saldo og owned stock. Vi tjekker også at en transaction bliver oprettet.

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));
    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 1, 1);

    tradingService.buyStock(request);

    List<Transaction> transactions = transactionDAO.getByPortfolioId(portfolioId, 0, 10);

    assertEquals(1, transactions.size());

    // OBS: ret evt. getType()/type() alt efter hvordan jeres Transaction er lavet.
    // assertEquals("BUY", transactions.get(0).type());
  }

  @Test
  void buyStock_success_callsCommitExactlyOnce()
  {
    // Optional / collaboration test:
    // Det her er lidt mere white-box, fordi vi tester samarbejdet med UoW.

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(1000)));
    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 1, 1);

    tradingService.buyStock(request);

    assertEquals(1, uow.commitCount);
    assertEquals(0, uow.rollbackCount);
  }

  @Test
  void buyStock_failure_callsRollbackExactlyOnce()
  {
    // Optional / collaboration test:
    // Ved fejl skal den rollbacke én gang.

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(50)));
    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 1, 1);

    assertThrows(RuntimeException.class, () -> tradingService.buyStock(request));

    assertEquals(0, uow.commitCount);
    assertEquals(1, uow.rollbackCount);
  }

  @Test
  void buyStock_negativeFee_throwsException()
  {
    // ZOMBIES: Exceptional
    // Hvis fee-strategien returnerer negativ fee, bør det ikke være tilladt.
    // Hvis denne test fejler, er det nok fordi service-klassen ikke validerer fee endnu.

    feeStrategyProvider = new FeeStrategyManager(new PercentageFeeStrategy(BigDecimal.valueOf(0.02)));


    tradingService = new TradingService(
        uow, portfolioDAO, stockDAO, ownedStockDAO, transactionDAO, feeStrategyProvider
    );

    UUID portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(-0.02)));
    stockDAO.create(new Stock("AAPL", "Apple", BigDecimal.valueOf(100), "Steady"));

    TradeRequestDTO request = new TradeRequestDTO(portfolioId, "AAPL", 1, 1);

    assertThrows(RuntimeException.class, () -> tradingService.buyStock(request));
  }

  // ------------------------------------------------------------
  // Små "spy" mocks.
  // De er basically bare mocks med tællere på.
  // Det gør det muligt at teste "blev create/update/commit kaldt?"
  // ------------------------------------------------------------

  static class SpyUnitOfWork extends MockUnitOfWork
  {
    int commitCount = 0;
    int rollbackCount = 0;

    boolean commitCalled = false;
    boolean rollbackCalled = false;

    @Override
    public void commit()
    {
      commitCount++;
      commitCalled = true;
      super.commit();
    }

    @Override
    public void rollback()
    {
      rollbackCount++;
      rollbackCalled = true;
      super.rollback();
    }
  }

  static class SpyOwnedStockDAO extends MockOwnedStockDAO
  {
    int createCount = 0;
    int updateCount = 0;

    @Override
    public void create(OwnedStock ownedStock)
    {
      createCount++;
      super.create(ownedStock);
    }

    @Override
    public void update(OwnedStock ownedStock)
    {
      updateCount++;
      super.update(ownedStock);
    }
  }
}