package presentation.core;

import business.services.*;
import business.services.notifications.StockAlertPublisher;
import business.stockmarket.MarketTickHandler;
import business.stockmarket.StockMarket;
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

  private final GameStateService gameStateService;
  private final PortfolioService portfolioService;
  private final TradingService tradingService;
  private final MarketTickHandler marketTickHandler;
  private final Thread marketThread;

  private final NotificationService notificationService;

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

    this.notificationService = new NotificationServiceImpl();

    StockMarket stockMarket = StockMarket.getInstance();
    StockSetupService stockSetupService = new StockSetupService(unitOfWork, stockDAO);

    this.gameStateService = new GameStateService(stockMarket, stockSetupService, stockDAO, false,
                                                 false);

    this.portfolioService = new PortfolioService(portfolioDAO, ownedStockDAO, transactionDAO,
                                                 stockDAO);

    this.tradingService = new TradingService(unitOfWork, portfolioDAO, stockDAO, ownedStockDAO,
                                             transactionDAO);

    this.userSession = new UserSession();
    PortfolioBootstrapService bootstrapService = new PortfolioBootstrapService(unitOfWork,
                                                                               portfolioDAO);
    UUID activePortfolioId = bootstrapService.getOrCreatePortfolioId();
    userSession.setActivePortfolioId(activePortfolioId);

    this.dashboardViewModel    = new DashboardViewModel(gameStateService);
    this.popUpWelcomeViewModel = new PopUpWelcomeViewModel(dashboardViewModel);
    this.portfolioViewModel    = new PortfolioViewModel(dashboardViewModel, portfolioService,
                                                        userSession);
    this.stockMarketViewModel  = new StockMarketViewModel(dashboardViewModel);
    this.buyStocksViewModel    = new BuyStocksViewModel(tradingService, portfolioService,
                                                        userSession);
    this.sellStocksViewModel   = new SellStocksViewModel(tradingService, portfolioService,
                                                         userSession);

    StockAlertPublisher alertPublisher = new StockAlertNotificationAdapter(notificationService);
    StockAlertService stockAlertService = new StockAlertService(ownedStockDAO, alertPublisher,
                                                                activePortfolioId);

    stockMarket.addListener(new StockPresentationListener(sellStocksViewModel));
    stockMarket.addListener(new StockPresentationListener(stockMarketViewModel));
    stockMarket.addListener(new StockPresentationListener(buyStocksViewModel));
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
}