import entities.Stock;
import persistence.fileimplementation.FileUnitOfWork;

import java.util.ArrayList;
import java.util.List;

public class TestMain{
  public static void main(String[] args)
  {
    FileUnitOfWork uow = new FileUnitOfWork("data/");

    List<Stock> stocks = uow.getStocks();

    for (Stock stock : stocks)
    {
      System.out.println("\u001B[25m"+stock.getSymbol() + " " + stock.getCurrentPrice()+"\u001B[0m");
    }


  }

}