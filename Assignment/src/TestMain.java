import business.stockmarket.MarketTickHandler;
import business.stockmarket.StockMarket;
import business.stockmarket.simulation.LiveStock;
import entities.Stock;
import persistence.fileimplementation.FileUnitOfWork;

import java.util.List;

public class TestMain{
  public static void main(String[] args) throws InterruptedException
  {
    FileUnitOfWork uow = new FileUnitOfWork("data/");

    StockMarket stockMarket = StockMarket.getInstance();

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

    MarketTickHandler thread = new MarketTickHandler();

    Thread ns = new Thread(thread);

    ns.start();

    Thread.sleep(10_000);
    ns.interrupt();

    ns.join();

  }

}