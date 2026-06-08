package IntegrationTest;

import business.dto.StockDTO;
import business.services.PortfolioService;
import business.services.StockPriceHistoryService;
import business.services.TradingService;
import business.services.interfaces.PortfolioServiceInterface;
import business.services.interfaces.StockPriceHistoryInterface;
import business.services.interfaces.TradingServiceInterface;
import business.strategies.fee.FeeCalculationStrategy;
import business.strategies.fee.PercentageFeeStrategy;
import entities.Portfolio;
import entities.Stock;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.*;
import persistence.fileimplementation.*;
import persistence.interfaces.*;
import presentation.state.UserSession;
import presentation.viewmodels.BuyStocksViewModel;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.Comparator;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuyStocksTest
{
  String testDirPath;

  private FileUnitOfWork uow;

  private PortfolioDAO portfolioDAO;
  private StockDAO stockDAO;
  private OwnedStockDAO ownedStockDAO;
  private TransactionDAO transactionDAO;
  private StockPriceHistoryDAO priceHistoryDAO;

  private TradingServiceInterface tradingServiceInterface;
  private PortfolioServiceInterface portfolioServiceInterface;
  private StockPriceHistoryInterface stockPriceHistoryInterface;

  private UserSession userSession;
  private UUID portfolioId;

  private BuyStocksViewModel buyViewModel;

  private StringProperty sharesInput;
  private StringProperty statusMessageOutput;

  private FeeCalculationStrategy feeCalculationStrategy;

  @BeforeAll
  static void initToolKit()
  {
    try
    {
      Platform.startup(() -> {});
    }
    catch (IllegalStateException ignored)
    {
    }
  }

  @BeforeEach
  public void setup()
  {
    testDirPath = "test-" + UUID.randomUUID();

    feeCalculationStrategy = new PercentageFeeStrategy(BigDecimal.valueOf(0.02));
    uow = new FileUnitOfWork(testDirPath);

    portfolioDAO = new PortfolioFileDAO(uow);
    stockDAO = new StockFileDAO(uow);
    ownedStockDAO = new OwnedStockFileDAO(uow);
    transactionDAO = new TransactionFileDAO(uow);
    priceHistoryDAO = new StockPriceHistoryFileDAO(uow);

    tradingServiceInterface =
        new TradingService(uow, portfolioDAO, stockDAO, ownedStockDAO, transactionDAO, feeCalculationStrategy);

    portfolioServiceInterface =
        new PortfolioService(portfolioDAO, ownedStockDAO, transactionDAO, stockDAO);

    stockPriceHistoryInterface =
        new StockPriceHistoryService(priceHistoryDAO);

    portfolioId = UUID.randomUUID();

    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(10000)));
    stockDAO.create(new Stock("APPL", "Apple", BigDecimal.valueOf(100), "Stable"));

    uow.commit();

    userSession = new UserSession();
    userSession.setActivePortfolioId(portfolioId);

    buyViewModel =
        new BuyStocksViewModel(tradingServiceInterface, portfolioServiceInterface,
                               stockPriceHistoryInterface, userSession);

    sharesInput = new SimpleStringProperty("");
    statusMessageOutput = new SimpleStringProperty("");

    sharesInput.bindBidirectional(buyViewModel.sharesProperty());
    statusMessageOutput.bind(buyViewModel.statusMessageProperty());
  }

  @AfterEach
  void cleanup() throws IOException
  {
    Path testFolder = Paths.get(testDirPath);

    if (Files.exists(testFolder))
    {
      Files.walk(testFolder)
           .sorted(Comparator.reverseOrder())
           .forEach(path -> {
             try
             {
               Files.delete(path);
             }
             catch (IOException e)
             {
               throw new RuntimeException(e);
             }
           });
    }
  }

  private StockDTO getAppleStock()
  {
    return portfolioServiceInterface.getAvailableStocks()
                                    .stream()
                                    .filter(stock -> stock.symbol().equalsIgnoreCase("APPL"))
                                    .findFirst()
                                    .orElseThrow();
  }

  private void buyShares(String numberOfShares)
  {
    buyViewModel.selectStock(getAppleStock());
    sharesInput.set(numberOfShares);
    buyViewModel.buy();
  }

  @Nested
  class GivenValidBuyOrder
  {
    @BeforeEach
    void act()
    {
      buyShares("5");
    }

    @Test
    void ownedStockIsCreated()
    {
      assertTrue(
          ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "APPL").isPresent()
      );
    }

    @Test
    void ownedStockHasCorrectQuantity()
    {
      int quantity =
          ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "APPL")
                       .orElseThrow()
                       .getNumberOfShares();

      assertEquals(5, quantity);
    }

    @Test
    void transactionIsStored()
    {
      assertEquals(1, transactionDAO.getAll().size());
    }

    @Test
    void transactionTypeIsBuy()
    {
      assertEquals("BUY", transactionDAO.getAll().get(0).type());
    }

    @Test
    void balanceIsReduced()
    {
      BigDecimal balance =
          portfolioDAO.getById(portfolioId)
                      .orElseThrow()
                      .getCurrentBalance();

      assertTrue(balance.compareTo(BigDecimal.valueOf(10000)) < 0);
    }
  }
}