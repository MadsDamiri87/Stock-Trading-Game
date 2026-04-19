import business.services.*;
import business.services.interfaces.GameStateServiceInterface;
import business.stockmarket.StockMarket;
import entities.Stock;
import persistence.fileimplementation.FileUnitOfWork;
import persistence.fileimplementation.OwnedStockFileDAO;
import persistence.fileimplementation.StockFileDAO;
import persistence.fileimplementation.StockPriceHistoryFileDAO;
import persistence.interfaces.OwnedStockDAO;
import persistence.interfaces.StockDAO;
import persistence.interfaces.StockPriceHistoryDAO;
import presentation.listeners.StockPresentationListener;
import presentation.viewmodels.DashboardViewModel;
import presentation.viewmodels.StockMarketViewModel;

import java.math.BigDecimal;
import java.util.Optional;

public class testclass
{
  public static void main(String[] args)
  {
    FileUnitOfWork uow = new FileUnitOfWork("data");

    StockDAO stockDAO = new StockFileDAO(uow);
    StockPriceHistoryDAO historyDAO = new StockPriceHistoryFileDAO(uow);

    StockMarket stockMarket = StockMarket.getInstance();

    OwnedStockDAO ownedStockDAO = new OwnedStockFileDAO(uow);

    StockListenerService stockListenerService =
        new StockListenerService(uow, stockDAO, historyDAO);
    StockBankruptService stockBankruptService = new StockBankruptService(uow, ownedStockDAO);
    StockAlertService stockAlertService = new StockAlertService();


    stockMarket.addListener(stockAlertService);
    stockMarket.addListener(stockListenerService);
    stockMarket.addListener(stockBankruptService);

    Optional<Stock> existing = stockDAO.getBySymbol("APPL");

    StockSetupService setupService = new StockSetupService(uow,stockDAO);

    Stock stock = setupService.getOrCreateStock("APPL", "Apple",
                                                BigDecimal.valueOf(150),
                                                "Steady");
    stockMarket.addExistingStock(stock);

    GameStateService gameStateService = new GameStateService(stockMarket, setupService, stockDAO, true, true);


    DashboardViewModel DBViewModel = new DashboardViewModel(gameStateService);
    StockMarketViewModel STMViewModel = new StockMarketViewModel(DBViewModel);
    StockPresentationListener uiListener = new StockPresentationListener(STMViewModel);


    stockMarket.addListener(uiListener);



  }
}