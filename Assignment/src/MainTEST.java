import entities.OwnedStock;
import entities.Portfolio;
import entities.Stock;
import persistence.fileimplementation.FileUnitOfWork;
import persistence.fileimplementation.OwnedStockFileDAO;
import persistence.fileimplementation.PortfolioFileDAO;
import persistence.fileimplementation.StockFileDAO;

import java.math.BigDecimal;
import java.util.UUID;

public class MainTEST
{
  public static void main(String[] args)
  {
    FileUnitOfWork uow = new FileUnitOfWork("data");

    StockFileDAO stockDao = new StockFileDAO(uow);
    PortfolioFileDAO portfolioDao = new PortfolioFileDAO(uow);
    OwnedStockFileDAO ownedStockDao = new OwnedStockFileDAO(uow);

    uow.beginTransaction();

    Stock apple = new Stock("AAPL", "APPLE", new BigDecimal("150.00"),
                            "ACTIVE");

    Portfolio portfolio = new Portfolio(UUID.randomUUID(),
                                        new BigDecimal("10000.00"));

    OwnedStock owned = new OwnedStock(UUID.randomUUID(),
                                      portfolio.getPortfolioId(),
                                      apple.getSymbol(), 10);

    if (stockDao.getBySymbol("AAPL").isEmpty())
    {
      stockDao.create(apple);
      System.out.println("Data gemt!");
    }
    else
    {
      System.out.println("Aktien eksisterer allerede");
    }
    portfolioDao.create(portfolio);
    ownedStockDao.create(owned);

    uow.commit();

    FileUnitOfWork uow2 = new FileUnitOfWork("data");

    StockFileDAO stockDao2 = new StockFileDAO(uow2);

    System.out.println("\nStocks i systemet: ");

    stockDao2.getAll().forEach(s -> System.out.println(
        s.getSymbol() + " - " + s.getName() + " - " + s.getCurrentPrice()));

    Stock updatedApple = new Stock("AAPL", "APPLE", new BigDecimal("200.00"),
                                   "ACTIVE");
    stockDao2.update(updatedApple);

    uow2.commit();

    System.out.println("\nEfter update:");

    stockDao2.getAll().forEach(s -> System.out.println(
        s.getSymbol() + " - " + s.getName() + " - " + s.getCurrentPrice()));
  }
}
