import business.stockmarket.MarketTickHandler;
import business.stockmarket.StockMarket;
import business.stockmarket.simulation.LiveStock;
import entities.Stock;
import persistence.fileimplementation.FileUnitOfWork;
import persistence.fileimplementation.StockPriceHistoryFileDAO;
import persistence.interfaces.StockPriceHistoryDAO;

import java.util.List;

public class TestMain{
  public static void main(String[] args) throws InterruptedException
  {
    FileUnitOfWork uow = new FileUnitOfWork("data/");


    StockPriceHistoryDAO dao = new StockPriceHistoryFileDAO(uow);
    StockMarket stockMarket = StockMarket.getInstance(dao);
    MarketTickHandler thread = new MarketTickHandler(stockMarket);
    StockPriceHistoryDAO stockPriceHistoryDAO = new StockPriceHistoryFileDAO(uow);


    List<Stock> stocks = uow.getStocks();

    for (Stock stock : stocks)
    {
      stockMarket.addExistingStock(stock);
      System.out.println("\u001B[25m"+stock.getSymbol() + " " + stock.getCurrentPrice()+"\u001B[0m");
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

  }

}