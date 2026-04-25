package IntegrationTest;

import business.dto.OwnedStockDTO;
import business.dto.StockDTO;
import business.services.*;
import business.services.interfaces.PortfolioServiceInterface;
import business.services.interfaces.StockPriceHistoryInterface;
import business.services.interfaces.TradingServiceInterface;
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
import presentation.viewmodels.SellStocksViewModel;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.Comparator;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SellStocksTest
{
  String testDirPath;

  FileUnitOfWork uow;
  PortfolioDAO portfolioDAO;
  StockDAO stockDAO;
  OwnedStockDAO ownedStockDAO;
  TransactionDAO transactionDAO;
  StockPriceHistoryDAO stockPriceHistoryDAO;

  PortfolioServiceInterface portfolioServiceInterface;
  StockPriceHistoryInterface stockPriceHistoryInterface;
  TradingServiceInterface tradingServiceInterface;

  UserSession userSession;
  UUID portfolioId;

  BuyStocksViewModel buyViewModel;
  SellStocksViewModel sellViewModel;

  StringProperty buySharesInput;
  StringProperty sellSharesInput;
  StringProperty sellStatusMessageOutput;

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
  void setup()
  {
    testDirPath = "test-" + UUID.randomUUID();

    uow = new FileUnitOfWork(testDirPath);

    portfolioDAO = new PortfolioFileDAO(uow);
    stockDAO = new StockFileDAO(uow);
    ownedStockDAO = new OwnedStockFileDAO(uow);
    transactionDAO = new TransactionFileDAO(uow);
    stockPriceHistoryDAO = new StockPriceHistoryFileDAO(uow);

    portfolioServiceInterface =
        new PortfolioService(portfolioDAO, ownedStockDAO, transactionDAO, stockDAO);

    stockPriceHistoryInterface =
        new StockPriceHistoryService(stockPriceHistoryDAO);

    tradingServiceInterface =
        new TradingService(uow, portfolioDAO, stockDAO, ownedStockDAO, transactionDAO);

    userSession = new UserSession();

    portfolioId = UUID.randomUUID();
    userSession.setActivePortfolioId(portfolioId);

    Portfolio portfolio = new Portfolio(portfolioId, BigDecimal.valueOf(10000));
    Stock stock = new Stock("GM", "General Motors", BigDecimal.valueOf(150), "Steady");

    portfolioDAO.create(portfolio);
    stockDAO.create(stock);

    uow.commit();

    sellViewModel =
        new SellStocksViewModel(tradingServiceInterface, portfolioServiceInterface,
                                stockPriceHistoryInterface, userSession);

    buyViewModel =
        new BuyStocksViewModel(tradingServiceInterface, portfolioServiceInterface,
                               stockPriceHistoryInterface, userSession);

    buySharesInput = new SimpleStringProperty("");
    sellSharesInput = new SimpleStringProperty("");
    sellStatusMessageOutput = new SimpleStringProperty("");

    buySharesInput.bindBidirectional(buyViewModel.sharesProperty());
    sellSharesInput.bindBidirectional(sellViewModel.sharesProperty());
    sellStatusMessageOutput.bind(sellViewModel.statusMessageProperty());
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

  private StockDTO getStockDTO()
  {
    return portfolioServiceInterface.getAvailableStocks()
                                    .stream()
                                    .filter(stock -> stock.symbol().equalsIgnoreCase("GM"))
                                    .findFirst()
                                    .orElseThrow();
  }

  private OwnedStockDTO getOwnedStockDTO()
  {
    return sellViewModel.getOwnedStocks()
                        .stream()
                        .filter(stock -> stock.stockSymbol().equalsIgnoreCase("GM"))
                        .findFirst()
                        .orElseThrow();
  }

  private void buyShares(String numberOfShares)
  {
    buyViewModel.selectStock(getStockDTO());
    buySharesInput.set(numberOfShares);
    buyViewModel.buy();
  }

  private void sellShares(String numberOfShares)
  {
    sellViewModel.loadPortfolioData();
    sellViewModel.selectOwnedStock(getOwnedStockDTO());
    sellSharesInput.set(numberOfShares);
    sellViewModel.sell();
  }

  @Nested
  class GivenValidSellOrder
  {
    @BeforeEach
    void act()
    {
      buyShares("10");
      sellShares("4");
    }

    @Test
    void ownedSharesAreReduced()
    {
      int remainingShares =
          ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "GM")
                       .orElseThrow()
                       .getNumberOfShares();

      assertEquals(6, remainingShares);
    }

    @Test
    void transactionIsStored()
    {
      assertEquals(2, transactionDAO.getAll().size());
    }

    @Test
    void latestTransactionTypeIsSell()
    {
      assertEquals("SELL", transactionDAO.getAll().get(1).type());
    }

    @Test
    void balanceIsIncreasedAfterSell()
    {
      BigDecimal balance =
          portfolioDAO.getById(portfolioId)
                      .orElseThrow()
                      .getCurrentBalance();

      assertTrue(balance.compareTo(BigDecimal.valueOf(10000 - 1500)) > 0);
    }
  }

  @Nested
  class GivenSellingAllShares
  {
    @BeforeEach
    void act()
    {
      buyShares("3");
      sellShares("3");
    }

    @Test
    void ownedStockIsDeleted()
    {
      assertTrue(
          ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "GM").isEmpty()
      );
    }
  }

  @Nested
  class GivenSellingMoreThanOwned
  {
    @BeforeEach
    void act()
    {
      buyShares("3");
      sellShares("5");
    }

    @Test
    void sharesRemainUnchanged()
    {
      int shares =
          ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "GM")
                       .orElseThrow()
                       .getNumberOfShares();

      assertEquals(3, shares);
    }

    @Test
    void noExtraTransactionIsCreated()
    {
      assertEquals(1, transactionDAO.getAll().size());
    }

    @Test
    void statusMessageShowsError()
    {
      assertEquals("Not enough shares to sell", sellStatusMessageOutput.get());
    }
  }
}