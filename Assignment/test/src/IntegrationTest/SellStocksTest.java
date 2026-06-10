package IntegrationTest;

import business.dto.OwnedStockDTO;
import business.dto.StockDTO;
import business.services.*;
import business.services.interfaces.PortfolioServiceInterface;
import business.services.interfaces.StockPriceHistoryInterface;
import business.services.interfaces.TradingServiceInterface;
import business.strategies.fee.*;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
  Integration / vertical slice test for salg af aktier.

  Det vigtige:
  - IKKE mocks.
  - Rigtige FileDAO'er + FileUnitOfWork.
  - Flere lag: ViewModel -> Service -> DAO -> FileUnitOfWork -> filer.

  Det betyder:
  - integrations / vertical slice-test.
*/

public class SellStocksTest
{
  String testDirPath;

  private FileUnitOfWork uow;

  private PortfolioDAO portfolioDAO;
  private StockDAO stockDAO;
  private OwnedStockDAO ownedStockDAO;
  private TransactionDAO transactionDAO;
  private StockPriceHistoryDAO stockPriceHistoryDAO;

  private PortfolioServiceInterface portfolioServiceInterface;
  private StockPriceHistoryInterface stockPriceHistoryInterface;
  private TradingServiceInterface tradingServiceInterface;

  private FeeStrategyProvider feeStrategyProvider;
  private FeeStrategySelector feeStrategySelector;

  private UserSession userSession;
  private UUID portfolioId;

  private BuyStocksViewModel buyViewModel;
  private SellStocksViewModel sellViewModel;

  private StringProperty buySharesInput;
  private StringProperty sellSharesInput;
  private StringProperty sellStatusMessageOutput;

  /*
    JavaFX skal startes, før ViewModel-properties kan bruges rigtigt.
    Hvis JavaFX allerede er startet, ignoreres fejlen.
  */
  @BeforeAll static void initToolKit()
  {
    try
    {
      Platform.startup(() -> {
      });
    }
    catch (IllegalStateException ignored)
    {
    }
  }

  /*
    KODEEKSEMPEL 1: Setup + Integration Test + FIRST

    Det her er Arrange-delen i AAA.

    Viser:
    - Integrationstest, fordi der bruges rigtige FileDAO'er.
    - FileUnitOfWork, som skriver til filer.
    - FIRST:
      - Independent: hver test får sin egen mappe.
      - Repeatable: hver test starter med samme kontrollerede setup.
    - Strategy Pattern:
      - FeeCalculationStrategy er abstraktionen.
      - PercentageFeeStrategy, FlatFeeStrategy og VolumeBasedFeeStrategy
        er konkrete strategier.
      - TradingService afhænger af FeeStrategyProvider og ikke konkrete strategier.

    ZOMBIES:
    - Zero:
      Testen starter uden gamle filer, transactions eller owned stocks.
    - Interfaces:
      Testen bruger ViewModelens public methods og service/DAO-interfaces.
    - Simple scenarios:
      Setup opretter kun det nødvendige:
      én portefølje, én aktie og én aktiv session.

    EP / Equivalence Partitioning:
    - Setup muliggør både gyldige og ugyldige salgsscenarier.
      Fx:
      - gyldigt salg: sælg færre aktier end man ejer
      - grænsetilfælde: sælg alle aktier
      - ugyldigt salg: sælg flere aktier end man ejer

    BVA / Boundary Value Analysis:
    - GivenSellingAllShares tester en vigtig boundary:
      remainingShares = 0.
    - GivenSellingMoreThanOwned tester lige over grænsen:
      forsøger at sælge mere end man ejer.
  */
  @BeforeEach void setup()
  {
    // ARRANGE

    // Hver test får sin egen mappe.
    // Det gør, at tests ikke deler filer eller gammel state.
    testDirPath = "test-" + UUID.randomUUID();

    // Rigtig FileUnitOfWork, altså ikke en mock.
    uow = new FileUnitOfWork(testDirPath);

    // Rigtige file-baserede DAO'er.
    portfolioDAO          = new PortfolioFileDAO(uow);
    stockDAO              = new StockFileDAO(uow);
    ownedStockDAO         = new OwnedStockFileDAO(uow);
    transactionDAO        = new TransactionFileDAO(uow);
    stockPriceHistoryDAO  = new StockPriceHistoryFileDAO(uow);

    /*
      Strategy setup:

      Her registreres de strategier, systemet kan vælge imellem.
      Default-strategien er Percentage.

      SOLID / DIP:
      TradingService får FeeStrategyProvider.
      Den kender derfor ikke PercentageFeeStrategy, FlatFeeStrategy
      eller VolumeBasedFeeStrategy direkte.

      Open/Closed:
      En ny strategi kan tilføjes ved at lave en ny klasse, der implementerer
      FeeCalculationStrategy, og registrere den i map'et.
      TradingService skal ikke ændres.
    */
    Map<String, FeeCalculationStrategy> feeStrategies = new HashMap<>();

    feeStrategies.put("Percentage", new PercentageFeeStrategy(new BigDecimal("0.02")));
    feeStrategies.put("Flat", new FlatFeeStrategy(new BigDecimal("10")));
    feeStrategies.put("Volume", new VolumeBasedFeeStrategy(new BigDecimal("0.25")));

    feeStrategyProvider = new FeeStrategyManager(feeStrategies.get("Percentage"));
    feeStrategySelector = new FeeStrategySelector(feeStrategyProvider, feeStrategies);

    // Services sættes op med de rigtige DAO'er.
    portfolioServiceInterface =
        new PortfolioService(portfolioDAO, ownedStockDAO, transactionDAO, stockDAO);

    stockPriceHistoryInterface =
        new StockPriceHistoryService(stockPriceHistoryDAO);

    tradingServiceInterface =
        new TradingService(uow, portfolioDAO, stockDAO, ownedStockDAO, transactionDAO,
                           feeStrategyProvider);

    // Sessionen peger på den portefølje, testen arbejder med.
    userSession = new UserSession();

    portfolioId = UUID.randomUUID();
    userSession.setActivePortfolioId(portfolioId);

    // Testdata: en portefølje og en aktie.
    Portfolio portfolio = new Portfolio(portfolioId, BigDecimal.valueOf(10000));
    Stock stock = new Stock("GM", "General Motors", BigDecimal.valueOf(150), "Steady");

    portfolioDAO.create(portfolio);
    stockDAO.create(stock);

    // Skriver startdata til filerne.
    // Det viser, at testen rammer persistence-laget.
    uow.commit();

    // Sell ViewModel er den primære ViewModel i denne test.
    sellViewModel =
        new SellStocksViewModel(tradingServiceInterface, portfolioServiceInterface,
                                stockPriceHistoryInterface, userSession);

    // Buy ViewModel bruges først til at købe aktier,
    // så sell-testen har noget at sælge.
    buyViewModel =
        new BuyStocksViewModel(tradingServiceInterface, portfolioServiceInterface,
                               stockPriceHistoryInterface, userSession,
                               feeStrategySelector);

    // Properties bruges til at simulere input/output fra UI'et.
    buySharesInput          = new SimpleStringProperty("");
    sellSharesInput         = new SimpleStringProperty("");
    sellStatusMessageOutput = new SimpleStringProperty("");

    buySharesInput.bindBidirectional(buyViewModel.sharesProperty());
    sellSharesInput.bindBidirectional(sellViewModel.sharesProperty());
    sellStatusMessageOutput.bind(sellViewModel.statusMessageProperty());
  }

  /*
    FIRST: cleanup

    Fordi testen skriver til filer, skal testmappen slettes bagefter.
    Ellers kan gamle testfiler påvirke nye tests.

    Det understøtter især:
    - Independent
    - Repeatable
  */
  @AfterEach void cleanup() throws IOException
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

  /*
    Finder GM-aktien gennem service-laget.

    Det er også en del af integrationstanken:
    testen bruger ikke bare en lokal variabel,
    men spørger systemet efter de tilgængelige aktier.

    ZOMBIES:
    - Interfaces:
      Testen bruger PortfolioServiceInterface til at hente aktier.
  */
  private StockDTO getStockDTO()
  {
    return portfolioServiceInterface.getAvailableStocks()
                                    .stream()
                                    .filter(stock -> stock.symbol().equalsIgnoreCase("GM"))
                                    .findFirst()
                                    .orElseThrow();
  }

  /*
    Finder den GM-aktie, som porteføljen ejer.

    Bruges efter buyShares(), fordi sell-flowet kræver,
    at aktien allerede findes som OwnedStock.

    ZOMBIES:
    - Interfaces:
      Testen bruger SellStocksViewModelens public metode getOwnedStocks().
  */
  private OwnedStockDTO getOwnedStockDTO()
  {
    return sellViewModel.getOwnedStocks()
                        .stream()
                        .filter(stock -> stock.stockSymbol().equalsIgnoreCase("GM"))
                        .findFirst()
                        .orElseThrow();
  }

  /*
    KODEEKSEMPEL 2: ACT helper for køb

    Det her er ikke en test i sig selv.
    Det er en helper-metode, der opbygger testens forudsætning.

    Flow:
    selectStock()
      -> brugeren vælger aktie

    buySharesInput.set(...)
      -> brugeren skriver antal

    buyViewModel.buy()
      -> køb starter gennem ViewModel
      -> TradingService kaldes
      -> DAO'er opdateres
      -> FileUnitOfWork skriver til filer
  */
  private void buyShares(String numberOfShares)
  {
    // ACT helper:
    buyViewModel.selectStock(getStockDTO());
    buySharesInput.set(numberOfShares);
    buyViewModel.buy();
  }

  /*
    KODEEKSEMPEL 3: ACT helper for salg

    Flow:
    loadPortfolioData()
      -> SellViewModel henter aktuelle owned stocks

    selectOwnedStock()
      -> brugeren vælger den aktie, der skal sælges

    sellSharesInput.set(...)
      -> brugeren skriver antal

    sellViewModel.sell()
      -> salg starter gennem ViewModel
      -> TradingService kaldes
      -> DAO'er opdateres
      -> FileUnitOfWork skriver til filer

    AAA:
    - Arrange sker i setup().
    - Act sker i nested @BeforeEach.
    - Assert sker i @Test-metoderne.
  */
  private void sellShares(String numberOfShares)
  {
    // ACT helper:
    sellViewModel.loadPortfolioData();
    sellViewModel.selectOwnedStock(getOwnedStockDTO());
    sellSharesInput.set(numberOfShares);
    sellViewModel.sell();
  }

  /*
    KODEEKSEMPEL 4: AAA + nested test

    Scenario:
    - Givet at brugeren ejer 10 aktier.
    - Når brugeren sælger 4.
    - Så skal der være 6 tilbage, og der skal oprettes en SELL transaction.

    ZOMBIES:
    - Many:
      Der købes 10 og sælges 4, altså flere aktier.
    - Simple behaviour:
      Testen fokuserer på ét normalt succes-scenarie.
    - Interfaces:
      Flowet går gennem ViewModels public methods.

    EP:
    - Gyldig partition:
      quantityToSell < ownedShares.
  */
  @Nested class GivenValidSellOrder
  {
    @BeforeEach void act()
    {
      // ACT
      buyShares("10");
      sellShares("4");
    }

    @Test void ownedSharesAreReduced()
    {
      // ASSERT
      // Efter salg af 4 ud af 10 skal der være 6 tilbage.
      int remainingShares =
          ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "GM")
                       .orElseThrow()
                       .getNumberOfShares();

      assertEquals(6, remainingShares);
    }

    @Test void transactionIsStored()
    {
      // ASSERT
      // Der bør være 2 transactions: én BUY og én SELL.
      assertEquals(2, transactionDAO.getAll().size());
    }

    @Test void latestTransactionTypeIsSell()
    {
      // ASSERT
      // Den seneste transaction skal være SELL.
      assertEquals("SELL", transactionDAO.getAll().get(1).type());
    }

    @Test void balanceIsIncreasedAfterSell()
    {
      // ASSERT
      // Saldoen skal være højere efter salget, end den var efter købet.
      BigDecimal balance =
          portfolioDAO.getById(portfolioId)
                      .orElseThrow()
                      .getCurrentBalance();

      assertTrue(balance.compareTo(BigDecimal.valueOf(10000 - 1500)) > 0);
    }
  }

  /*
    Scenario:
    - Givet at brugeren ejer 3 aktier.
    - Når brugeren sælger alle 3.
    - Så skal OwnedStock slettes helt.

    ZOMBIES:
    - Boundary behaviours:
      Dette tester grænsen hvor remainingShares bliver 0.
    - One/Many:
      Der arbejdes stadig med én aktietype, men flere shares.

    BVA:
    - remainingShares = 0 er en vigtig boundary.
      Ved 0 skal OwnedStock fjernes i stedet for bare at blive opdateret.
  */
  @Nested class GivenSellingAllShares
  {
    @BeforeEach void act()
    {
      // ACT
      buyShares("3");
      sellShares("3");
    }

    @Test void ownedStockIsDeleted()
    {
      // ASSERT
      // Når alle aktier sælges, skal OwnedStock fjernes helt.
      assertTrue(ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "GM").isEmpty());
    }
  }

  /*
    Scenario:
    - Givet at brugeren ejer 3 aktier.
    - Når brugeren prøver at sælge 5.
    - Så må salget ikke gennemføres.

    ZOMBIES:
    - Exceptions:
      Dette er et failure scenario, ikke happy path.
    - Boundary behaviours:
      Brugeren prøver at sælge mere end grænsen for hvad de ejer.
    - Simple behaviour:
      Testen kontrollerer at state ikke ændres forkert ved fejl.

    EP:
    - Ugyldig partition:
      quantityToSell > ownedShares.

    BVA:
    - Det interessante boundary-område er omkring ownedShares.
      Hvis ownedShares = 3, er 3 gyldigt, men 4 og 5 ugyldigt.
      Her tester vi klart udenfor den gyldige grænse.
  */
  @Nested class GivenSellingMoreThanOwned
  {
    @BeforeEach void act()
    {
      // ACT
      buyShares("3");
      sellShares("5");
    }

    @Test void sharesRemainUnchanged()
    {
      // ASSERT
      // Antallet af aktier skal stadig være 3, fordi salget ikke måtte gennemføres.
      int shares =
          ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "GM")
                       .orElseThrow()
                       .getNumberOfShares();

      assertEquals(3, shares);
    }

    @Test void noExtraTransactionIsCreated()
    {
      // ASSERT
      // Der må ikke oprettes en ekstra SELL transaction, når salget fejler.
      // Der findes kun den oprindelige BUY transaction.
      assertEquals(1, transactionDAO.getAll().size());
    }

    @Test void statusMessageShowsError()
    {
      // ASSERT
      // ViewModel'en skal vise en fejlbesked til brugeren.
      assertEquals("Not enough shares to sell", sellStatusMessageOutput.get());
    }
  }
}