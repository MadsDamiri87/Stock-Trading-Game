import business.services.StockAlertService;
import business.services.StockBankruptService;
import business.services.StockListenerService;
import business.stockmarket.MarketTickHandler;
import business.stockmarket.StockMarket;
import entities.Stock;
import persistence.fileimplementation.FileUnitOfWork;
import persistence.fileimplementation.OwnedStockFileDAO;
import persistence.fileimplementation.StockFileDAO;
import persistence.fileimplementation.StockPriceHistoryFileDAO;
import persistence.interfaces.OwnedStockDAO;
import persistence.interfaces.StockDAO;
import persistence.interfaces.StockPriceHistoryDAO;

import java.util.List;

public class TestMain
{
  public static void main(String[] args) throws InterruptedException
  {
    FileUnitOfWork uow = new FileUnitOfWork("data/");

    StockMarket stockMarket = StockMarket.getInstance();
    MarketTickHandler thread = new MarketTickHandler(stockMarket);

    List<Stock> stocks = uow.getStocks();

    for (Stock stock : stocks)
    {
      stockMarket.addExistingStock(stock);
      System.out.println(
          "\u001B[25m" + stock.getSymbol() + " " + stock.getCurrentPrice()
              + "\u001B[0m");
    }

    for (int i = 0; i < 10; i++)
    {
      stockMarket.updateAllStocks();
    }

    Thread ns = new Thread(thread);

    ns.start();

    Thread.sleep(10_000);
    ns.interrupt();

    ns.join();

    uow.commit();


    StockDAO stockDAO = new StockFileDAO(uow);
    StockPriceHistoryDAO historyDAO = new StockPriceHistoryFileDAO(uow);

    StockMarket stockMarket1 = StockMarket.getInstance();

    OwnedStockDAO ownedStockDAO = new OwnedStockFileDAO(uow);

    StockListenerService stockListenerService = new StockListenerService(uow, stockDAO, historyDAO);
    StockBankruptService stockBankruptService = new StockBankruptService(uow, ownedStockDAO);
    StockAlertService stockAlertService = new StockAlertService();

    stockMarket1.addListener(stockListenerService);
    stockMarket1.addListener(stockBankruptService);
    stockMarket1.addListener(stockAlertService);

    stockMarket1.updateAllStocks();




  }

}