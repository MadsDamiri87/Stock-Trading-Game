package presentation.core;

import business.services.*;
import business.services.interfaces.GameStateServiceInterface;
import business.services.interfaces.PortfolioServiceInterface;
import business.services.interfaces.StockPriceHistoryInterface;
import business.services.interfaces.TradingServiceInterface;
import business.services.notifications.StockAlertPublisher;
import business.stockmarket.MarketTickHandler;
import business.stockmarket.StockMarket;
import business.strategies.fee.*;
import persistence.fileimplementation.*;
import persistence.interfaces.*;
import presentation.listeners.StockPresentationListener;
import presentation.notifications.NotificationService;
import presentation.notifications.NotificationServiceImpl;
import presentation.notifications.StockAlertNotificationAdapter;
import presentation.state.UserSession;
import presentation.viewmodels.BuyStocksViewModel;
import presentation.viewmodels.DashboardViewModel;
import presentation.viewmodels.PopUpWelcomeViewModel;
import presentation.viewmodels.PortfolioViewModel;
import presentation.viewmodels.SellStocksViewModel;
import presentation.viewmodels.StockMarketViewModel;
import shared.logging.FileLogOutputAdapter;
import shared.logging.Logger;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ApplicationContext
{
  private final DashboardViewModel dashboardViewModel;
  private final PopUpWelcomeViewModel popUpWelcomeViewModel;
  private final PortfolioViewModel portfolioViewModel;
  private final StockMarketViewModel stockMarketViewModel;
  private final BuyStocksViewModel buyStocksViewModel;
  private final SellStocksViewModel sellStocksViewModel;

  private final UnitOfWork unitOfWork;
  private final StockDAO stockDAO;
  private final PortfolioDAO portfolioDAO;
  private final OwnedStockDAO ownedStockDAO;
  private final TransactionDAO transactionDAO;
  private final StockPriceHistoryDAO stockPriceHistoryDAO;

  private final StockPriceHistoryInterface stockHistoryService;
  private final GameStateServiceInterface gameStateService;
  private final PortfolioServiceInterface portfolioService;
  private final TradingServiceInterface tradingService;
  private final MarketTickHandler marketTickHandler;
  private final Thread marketThread;

  private final FeeStrategyProvider feeStrategyProvider;
  private final FeeStrategySelector feeStrategySelector;

  private final NotificationService notificationService;
  private final NavigationService navigationService;

  private final UserSession userSession;

  public ApplicationContext()
  {
    FileUnitOfWork fileUnitOfWork = new FileUnitOfWork("data");
    this.unitOfWork = fileUnitOfWork;

    this.stockDAO             = new StockFileDAO(fileUnitOfWork);
    this.portfolioDAO         = new PortfolioFileDAO(fileUnitOfWork);
    this.ownedStockDAO        = new OwnedStockFileDAO(fileUnitOfWork);
    this.transactionDAO       = new TransactionFileDAO(fileUnitOfWork);
    this.stockPriceHistoryDAO = new StockPriceHistoryFileDAO(fileUnitOfWork);


//    Strategy pattern:
    Map<String, FeeCalculationStrategy> feeStrategies = new HashMap<>();

    feeStrategies.put("Percentage", new PercentageFeeStrategy(BigDecimal.valueOf(0.04)));
    feeStrategies.put("Flat", new FlatFeeStrategy(BigDecimal.valueOf(10)));
    feeStrategies.put("Volume", new VolumeBasedFeeStrategy(BigDecimal.valueOf(0.25)));

    this.feeStrategyProvider = new FeeStrategyManager(feeStrategies.get("Percentage"));

    this.feeStrategySelector = new FeeStrategySelector(feeStrategyProvider, feeStrategies);

//    Strategy slut


    //    det der er "importeret fra Troels".
    //    this.notificationService = new CustomAlertBoxAdapter();
    //    Min egen:
    this.notificationService = new NotificationServiceImpl();

    StockMarket stockMarket = StockMarket.getInstance();
    StockSetupService stockSetupService = new StockSetupService(unitOfWork, stockDAO);

    this.stockHistoryService = new StockPriceHistoryService(stockPriceHistoryDAO);
    this.gameStateService    = new GameStateService(stockMarket, stockSetupService, stockDAO, false,
                                                    false);

    this.portfolioService = new PortfolioService(portfolioDAO, ownedStockDAO, transactionDAO,
                                                 stockDAO);

    this.tradingService = new TradingService(unitOfWork, portfolioDAO, stockDAO, ownedStockDAO,
                                             transactionDAO, feeStrategyProvider);

    Logger.getInstance().setOutput(new FileLogOutputAdapter("logs/application.log", "INFO"));

    this.userSession = new UserSession();
    PortfolioBootstrapService bootstrapService = new PortfolioBootstrapService(unitOfWork,
                                                                               portfolioDAO);
    UUID activePortfolioId = bootstrapService.getOrCreatePortfolioId();
    userSession.setActivePortfolioId(activePortfolioId);

    this.dashboardViewModel = new DashboardViewModel(gameStateService);
    this.navigationService  = new NavigationServiceAdapter(dashboardViewModel);

    this.popUpWelcomeViewModel = new PopUpWelcomeViewModel(navigationService);
    this.portfolioViewModel    = new PortfolioViewModel(navigationService, portfolioService,
                                                        userSession);
    this.stockMarketViewModel  = new StockMarketViewModel(navigationService, stockHistoryService);
    this.buyStocksViewModel    = new BuyStocksViewModel(tradingService, portfolioService,
                                                        stockHistoryService, userSession,
                                                        feeStrategySelector);

    this.sellStocksViewModel = new SellStocksViewModel(tradingService, portfolioService,
                                                       stockHistoryService, userSession);

    StockAlertPublisher alertPublisher = new StockAlertNotificationAdapter(notificationService);
    StockAlertService stockAlertService = new StockAlertService(ownedStockDAO, alertPublisher,
                                                                activePortfolioId);

    stockMarket.addListener(new StockPresentationListener(sellStocksViewModel));
    stockMarket.addListener(new StockPresentationListener(stockMarketViewModel));
    stockMarket.addListener(new StockPresentationListener(buyStocksViewModel));
    stockMarket.addListener(new StockPresentationListener(portfolioViewModel));
    stockMarket.addListener(new StockListenerService(unitOfWork, stockDAO, stockPriceHistoryDAO));
    stockMarket.addListener(new StockBankruptService(unitOfWork, ownedStockDAO));
    stockMarket.addListener(stockAlertService);

    this.marketTickHandler = new MarketTickHandler(gameStateService);
    this.marketThread      = new Thread(marketTickHandler);
    this.marketThread.setDaemon(true);

    gameStateService.startGame();
    marketThread.start();

  }

  public PopUpWelcomeViewModel getPopUpWelcomeViewModel()
  {
    return popUpWelcomeViewModel;
  }

  public DashboardViewModel getDashboardViewModel()
  {
    return dashboardViewModel;
  }

  public PortfolioViewModel getPortfolioViewModel()
  {
    return portfolioViewModel;
  }

  public StockMarketViewModel getStockMarketViewModel()
  {
    return stockMarketViewModel;
  }

  public BuyStocksViewModel getBuyStocksViewModel()
  {
    return buyStocksViewModel;
  }

  public SellStocksViewModel getSellStocksViewModel()
  {
    return sellStocksViewModel;
  }

  public NotificationService getNotificationService()
  {
    return notificationService;
  }

  public NavigationService getNavigationService()
  {
    return navigationService;
  }
}