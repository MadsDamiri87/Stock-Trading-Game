import business.services.StockAlertService;
import business.services.StockBankruptService;
import business.services.StockListenerService;
import business.stockmarket.StockMarket;
import persistence.fileimplementation.FileUnitOfWork;
import persistence.fileimplementation.StockFileDAO;
import persistence.fileimplementation.StockPriceHistoryFileDAO;
import persistence.interfaces.StockDAO;
import persistence.interfaces.StockPriceHistoryDAO;

public class MainClass
{
  public static void main(String[] args)
  {
    StockMarket stockMarket1 = StockMarket.getInstance();

    FileUnitOfWork uow = new FileUnitOfWork("data");

    StockDAO stockDAO = new StockFileDAO(uow);

    StockPriceHistoryDAO historyDAO = new StockPriceHistoryFileDAO(uow);

    StockListenerService stockListenerService = new StockListenerService(uow, stockDAO, historyDAO);
    StockBankruptService stockBankruptService = new StockBankruptService();
    StockAlertService stockAlertService = new StockAlertService();

    stockMarket1.addListener(stockListenerService);
    stockMarket1.addListener(stockBankruptService);
    stockMarket1.addListener(stockAlertService);

    stockMarket1.updateAllStocks();
  }
}
