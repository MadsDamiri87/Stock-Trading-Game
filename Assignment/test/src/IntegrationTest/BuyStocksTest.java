package IntegrationTest;

import business.dto.StockDTO;
import business.services.PortfolioService;
import business.services.StockPriceHistoryService;
import business.services.TradingService;
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
  Integration / vertical slice test for køb af aktier.

  Det vigtige:
  - IKKE mocks.
  - Rigtige FileDAO'er + FileUnitOfWork.
  - Flere lag: ViewModel -> Service -> DAO -> FileUnitOfWork -> filer.

  Det betyder:
  - integrations / vertical slice-test.
*/

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

  private FeeStrategyProvider feeStrategyProvider;
  private FeeStrategySelector feeStrategySelector;

  private UserSession userSession;
  private UUID portfolioId;

  private BuyStocksViewModel buyViewModel;

  private StringProperty sharesInput;
  private StringProperty statusMessageOutput;

  /*
    KODEEKSEMPEL 2: AAA + nested test

    Arrange:
    - setup() = opretter testmiljøet.

    Act:
    - buyShares("5") køres i @BeforeEach i nested-klassen.

    Assert:
    - selve @Test-metoderne kontrollerer resultatet.

    ZOMBIES:
    - One:
      Vi køber én konkret type aktie: APPL.
    - Many:
      Vi køber 5 aktier, altså mere end én.
    - Simple behaviour:
      Testen tester ét simpelt succes-scenarie:
      en gyldig købsordre opretter owned stock, transaction og reducerer balance.

    Hvorfor nested?
    - Det samler tests omkring samme scenarie.
    - Her betyder GivenValidBuyOrder:
      "Når der er lavet en gyldig købsordre".
  */
  @Nested class GivenValidBuyOrder
  {
    @BeforeEach void act()
    {
      // ACT:
      // selve use casen: køb 5 aktier.
      buyShares("5");
    }

    @Test void ownedStockHasCorrectQuantity()
    {
      // ASSERT
      // Efter købet forventer vi, at porteføljen ejer 5 Apple-aktier.
      int quantity = ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "APPL")
                                  .orElseThrow()
                                  .getNumberOfShares();

      assertEquals(5, quantity);
    }

    @Test void ownedStockIsCreated()
    {
      // ASSERT
      // Findes som en ejet aktie i porteføljen?
      assertTrue(ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, "APPL").isPresent());
    }

    @Test void transactionIsStored()
    {
      // ASSERT
      // Købet skal oprette én transaction i persistence-laget.
      assertEquals(1, transactionDAO.getAll().size());
    }

    @Test void transactionTypeIsBuy()
    {
      // ASSERT
      // Den oprettede transaction skal være af typen BUY.
      assertEquals("BUY", transactionDAO.getAll().get(0).type());
    }

    @Test void balanceIsReduced()
    {
      // ASSERT
      // Saldoen skal være lavere end startbalancen efter købet.
      BigDecimal balance = portfolioDAO.getById(portfolioId).orElseThrow().getCurrentBalance();

      assertTrue(balance.compareTo(BigDecimal.valueOf(10000)) < 0);
    }
  }

  /*
    KODEEKSEMPEL 1: Setup + Integration Test + FIRST

    Det her er det vigtigste setup i testen.

    Viser:
    - Integrationstest, fordi der bruges rigtige FileDAO'er.
    - FileUnitOfWork, som skriver til filer.
    - FIRST:
      - Independent: hver test får sin egen mappe.
      - Repeatable: testene starter fra samme kontrollerede setup.
    - Arrange-delen i AAA.

    ZOMBIES:
    - Zero:
      Testen starter uden gamle filer, transactions eller owned stocks.
    - Interfaces:
      Testen går primært gennem offentlige metoder:
      ViewModel.buy(), selectStock(), DAO-interface-metoder og service-interfaces.
    - Simple scenarios:
      Setup laver kun det nødvendige:
      én portefølje, én aktie og én aktiv session.

    EP / Equivalence Partitioning:
    - Denne test bruger en gyldig partition:
      quantity = 5, aktiv portefølje, eksisterende aktie og nok balance.

    BVA / Boundary Value Analysis:
    - Ikke hovedfokus i denne test.
      Boundary-cases som quantity = 0, quantity = -1 eller insufficient funds
      bør ligge i separate tests.
  */
  @BeforeEach public void setup()
  {
    // ARRANGE

    // Hver test får sin egen mappe.
    // Det gør, at tests ikke deler filer eller gammel state.
    testDirPath = "test-" + UUID.randomUUID();

    // Rigtig UnitOfWork, som arbejder med filer.
    uow = new FileUnitOfWork(testDirPath);

    // Rigtige file-baserede DAO'er.
    // ingen mocks.
    portfolioDAO    = new PortfolioFileDAO(uow);
    stockDAO        = new StockFileDAO(uow);
    ownedStockDAO   = new OwnedStockFileDAO(uow);
    transactionDAO  = new TransactionFileDAO(uow);
    priceHistoryDAO = new StockPriceHistoryFileDAO(uow);

    /*
      Strategy setup:

      FeeCalculationStrategy er selve Strategy-interfacet.
      PercentageFeeStrategy, FlatFeeStrategy og VolumeBasedFeeStrategy
      er konkrete strategier.

      FeeStrategyProvider holder den aktive strategi.
      FeeStrategySelector vælger en strategi ud fra et navn.

      SOLID / DIP:
      TradingService afhænger af FeeStrategyProvider,
      ikke af konkrete strategy-klasser.

      Open/Closed:
      Hvis der tilføjes en ny fee-strategi, skal TradingService ikke ændres.
      Den nye strategi registreres her i setup/composition root.
    */
    Map<String, FeeCalculationStrategy> feeStrategies = new HashMap<>();

    feeStrategies.put("Percentage", new PercentageFeeStrategy(new BigDecimal("0.04")));
    feeStrategies.put("Flat", new FlatFeeStrategy(new BigDecimal("10")));
    feeStrategies.put("Volume", new VolumeBasedFeeStrategy(new BigDecimal("0.25")));

    feeStrategyProvider = new FeeStrategyManager(feeStrategies.get("Percentage"));
    feeStrategySelector = new FeeStrategySelector(feeStrategyProvider, feeStrategies);

    // Service-laget sættes op med de rigtige DAO'er.
    tradingServiceInterface = new TradingService(uow, portfolioDAO, stockDAO, ownedStockDAO,
                                                 transactionDAO, feeStrategyProvider);

    portfolioServiceInterface = new PortfolioService(portfolioDAO, ownedStockDAO, transactionDAO,
                                                     stockDAO);

    stockPriceHistoryInterface = new StockPriceHistoryService(priceHistoryDAO);

    // Testdata: en portefølje med 10.000.
    portfolioId = UUID.randomUUID();
    portfolioDAO.create(new Portfolio(portfolioId, BigDecimal.valueOf(10000)));

    // Testdata: en aktie som kan købes.
    stockDAO.create(new Stock("APPL", "Apple", BigDecimal.valueOf(100), "Stable"));

    // Her skrives startdata til testfilerne.
    // Det er et vigtigt tegn på, at testen faktisk rammer persistence-laget.
    uow.commit();

    // UserSession fortæller ViewModel'en hvilken portefølje der er aktiv.
    userSession = new UserSession();
    userSession.setActivePortfolioId(portfolioId);

    // ViewModel'en sættes op.
    // Testen går altså ikke direkte på TradingService, men gennem ViewModel'en.
    buyViewModel = new BuyStocksViewModel(tradingServiceInterface, portfolioServiceInterface,
                                          stockPriceHistoryInterface, userSession,
                                          feeStrategySelector);

    // Properties simulerer input/output fra UI-laget.
    sharesInput         = new SimpleStringProperty("");
    statusMessageOutput = new SimpleStringProperty("");

    sharesInput.bindBidirectional(buyViewModel.sharesProperty());
    statusMessageOutput.bind(buyViewModel.statusMessageProperty());
  }

  /*
    KODEEKSEMPEL 3: Selve use case-flowet

    Det her er ikke en test i sig selv.
    Det er en helper-metode.

    Den er god at vise, fordi den viser vertical slice-flowet:

    selectStock()
      -> brugeren vælger aktie

    sharesInput.set(...)
      -> brugeren skriver antal

    buyViewModel.buy()
      -> use casen starter gennem ViewModel'en
      -> TradingService kaldes
      -> FeeStrategyProvider leverer aktiv FeeCalculationStrategy
      -> DAO'er opdateres
      -> FileUnitOfWork skriver til filer

    AAA:
    - Arrange sker i setup().
    - Act sker her.
    - Assert sker i @Test-metoderne.

    ZOMBIES:
    - Interfaces:
      Vi tester gennem ViewModelens public API i stedet for private metoder.
    - Simple behaviour:
      Helperen gør kun én ting: gennemfører et køb.
  */
  private void buyShares(String numberOfShares)
  {
    buyViewModel.selectStock(getAppleStock());
    sharesInput.set(numberOfShares);
    buyViewModel.buy();
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
    Finder Apple-aktien gennem service-laget.

    Det er også en del af integrationstanken:
    testen bruger ikke bare en lokal variabel,
    men spørger systemet efter de tilgængelige aktier.

    ZOMBIES:
    - Interfaces:
      Testen bruger PortfolioServiceInterface til at hente aktier,
      i stedet for at gå udenom systemets offentlige adgangsvej.
  */
  private StockDTO getAppleStock()
  {
    return portfolioServiceInterface.getAvailableStocks().stream()
                                    .filter(stock -> stock.symbol().equalsIgnoreCase("APPL"))
                                    .findFirst().orElseThrow();
  }
}