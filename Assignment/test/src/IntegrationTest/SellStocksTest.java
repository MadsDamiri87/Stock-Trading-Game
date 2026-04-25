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
import org.junit.jupiter.api.*;
import persistence.fileimplementation.*;
import persistence.interfaces.*;
import presentation.state.UserSession;
import presentation.viewmodels.BuyStocksViewModel;
import presentation.viewmodels.SellStocksViewModel;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  @BeforeAll static void initToolKit()
  {
    Platform.startup(() -> {
    });
  }

  @BeforeEach void setup()
  {
    testDirPath = "test-" + UUID.randomUUID();

    uow = new FileUnitOfWork(testDirPath);

    portfolioDAO         = new PortfolioFileDAO(uow);
    stockDAO             = new StockFileDAO(uow);
    ownedStockDAO        = new OwnedStockFileDAO(uow);
    transactionDAO       = new TransactionFileDAO(uow);
    stockPriceHistoryDAO = new StockPriceHistoryFileDAO(uow);

    portfolioServiceInterface  = new PortfolioService(portfolioDAO, ownedStockDAO, transactionDAO,
                                                      stockDAO);
    stockPriceHistoryInterface = new StockPriceHistoryService(stockPriceHistoryDAO);
    tradingServiceInterface    = new TradingService(uow, portfolioDAO, stockDAO, ownedStockDAO,
                                                    transactionDAO);
    userSession                = new UserSession();

    portfolioId = UUID.randomUUID();
    Portfolio portfolio = new Portfolio(portfolioId, BigDecimal.valueOf(10000));

    userSession.setActivePortfolioId(portfolioId);

    portfolioDAO.create(portfolio);

    Stock stock = new Stock("GM", "General Motors", BigDecimal.valueOf(150), "Steady");
    stockDAO.create(stock);

    uow.commit();

    sellViewModel = new SellStocksViewModel(tradingServiceInterface, portfolioServiceInterface,
                                            stockPriceHistoryInterface, userSession);
    buyViewModel = new BuyStocksViewModel(tradingServiceInterface, portfolioServiceInterface,
                                          stockPriceHistoryInterface, userSession);
  }

  @AfterEach void cleanup() throws IOException
  {
    Path testFolder = Paths.get(testDirPath);

    if (Files.exists(testFolder))
    {
      Files.walk(testFolder).sorted(Comparator.reverseOrder()).forEach(path -> {
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

  @Nested class GivenValidSellOrder
  {
    @BeforeEach void act()
    {
      StockDTO stock = portfolioServiceInterface.getAvailableStocks().stream()
                                                .filter(s -> s.symbol().equalsIgnoreCase("GM"))
                                                .findFirst().orElseThrow();

      buyViewModel.selectStock(stock);
      buyViewModel.sharesProperty().set("10");
      buyViewModel.buy();

      sellViewModel.loadPortfolioData();

      OwnedStockDTO ownedStock = sellViewModel.getOwnedStocks().stream()
                                              .filter(s -> s.stockSymbol().equalsIgnoreCase("GM"))
                                              .findFirst().orElseThrow();

      sellViewModel.selectOwnedStock(ownedStock);
      sellViewModel.sharesProperty().set("4");
      sellViewModel.sell();
    }

    @Test void ownedSharesAreReduced()
    {
      int remainingShares = ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "GM")
                                         .orElseThrow().getNumberOfShares();

      assertEquals(6, remainingShares);
    }

    @Test void transactionIsStored()
    {
      assertEquals(2, transactionDAO.getAll().size());
    }

    @Test void latestTransactionTypeIsSell()
    {
      assertEquals("SELL", transactionDAO.getAll().get(1).type());
    }

    @Test void balanceIsIncreasedAfterSell()
    {
      BigDecimal balance = portfolioDAO.getById(portfolioId).orElseThrow().getCurrentBalance();

      assertTrue(balance.compareTo(BigDecimal.valueOf(10000 - 1500)) > 0);

    }
  }

  @Nested class GivenSellingAllShares
  {
    @BeforeEach void act()
    {
      StockDTO stockDTO = portfolioServiceInterface.getAvailableStocks().stream()
                                                   .filter(s -> s.symbol().equalsIgnoreCase("GM"))
                                                   .findFirst().orElseThrow();
      buyViewModel.selectStock(stockDTO);
      buyViewModel.sharesProperty().set("3");
      buyViewModel.buy();

      sellViewModel.loadPortfolioData();

      OwnedStockDTO ownedStock = sellViewModel.getOwnedStocks().stream()
                                              .filter(s -> s.stockSymbol().equalsIgnoreCase("GM"))
                                              .findFirst().orElseThrow();

      sellViewModel.selectOwnedStock(ownedStock);
      sellViewModel.sharesProperty().set("3");
      sellViewModel.sell();
    }

    @Test void ownedStocksIsDeleted()
    {
      assertTrue(ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "GM").isEmpty());
    }
  }

  @Nested class GivenSellingMoreThanOwned
  {
    @BeforeEach void act()
    {
      StockDTO stockDTO = portfolioServiceInterface.getAvailableStocks().stream()
                                                   .filter(s -> s.symbol().equalsIgnoreCase("GM"))
                                                   .findFirst().orElseThrow();
      buyViewModel.selectStock(stockDTO);
      buyViewModel.sharesProperty().set("3");
      buyViewModel.buy();

      sellViewModel.loadPortfolioData();

      OwnedStockDTO ownedStock = sellViewModel.getOwnedStocks().stream()
                                              .filter(s -> s.stockSymbol().equalsIgnoreCase("GM"))
                                              .findFirst().orElseThrow();
      sellViewModel.selectOwnedStock(ownedStock);
      sellViewModel.sharesProperty().set("5");
      sellViewModel.sell();
    }

    @Test void sharesRemainUnchanged()
    {
      int shares = ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "GM").orElseThrow()
                                .getNumberOfShares();

      assertEquals(3, shares);
    }

    @Test void noExtraTransactionIsCreated()
    {
      assertEquals(1, transactionDAO.getAll().size());
    }

    @Test void statusMessageShowsError()
    {
      assertEquals("You cannot sell more shares than you own.",
                   sellViewModel.statusMessageProperty().get());

//            acceptabel fejl:
//      Expected :You cannot sell more shares than you own.
//      Actual   :Not enough shares to sell
    }
  }
}
