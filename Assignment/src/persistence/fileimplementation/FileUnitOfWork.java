package persistence.fileimplementation;

import entities.OwnedStock;
import entities.Portfolio;
import entities.Stock;
import persistence.interfaces.UnitOfWork;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUnitOfWork implements UnitOfWork

{

  private final String directoryPath;

  private List<Portfolio> portfolios;
  private List<Stock> stocks;
  private List<OwnedStock> ownedStocks;

  private static final String PORTFOLIO_FILE = "portfolios.psv";
  private static final String STOCK_FILE = "stocks.psv";
  private static final String OWNEDSTOCK_FILE = "ownedstocks.psv";

  public FileUnitOfWork(String directoryPath)
  {
    this.directoryPath = directoryPath;
    ensureFilesExist();
  }

  @Override public void beginTransaction()
  {
    resetLists();
  }

  @Override public synchronized void commit()
  {
    if (portfolios != null)
    {
      writePortfoliosToFile();
    }
    if (stocks != null)
    {
      writeStocksToFile();
    }
    if (ownedStocks != null)
    {
      writeOwnedStocksToFile();
    }
    resetLists();
  }

  private void writePortfoliosToFile()
  {
    Path filePath = Paths.get(directoryPath, PORTFOLIO_FILE);

    List<String> lines = new ArrayList<>();

    for (Portfolio p : portfolios)
    {
      lines.add(toPSV(p));
    }
    try
    {
      Files.write(filePath, lines);
    }
    catch (IOException e)
    {
      throw new RuntimeException("Fejl ved skrivningen af portfolios: ", e);
    }
  }

  private void writeStocksToFile()
  {
    Path filePath = Paths.get(directoryPath, STOCK_FILE);

    List<String> lines = new ArrayList<>();

    for (Stock s : stocks)
    {
      lines.add(toPSV(s));
    }
    try
    {
      Files.write(filePath, lines);
    }
    catch (IOException e)
    {
      throw new RuntimeException("Fejl ved skrivningen af stocks: ", e);
    }
  }

  private void writeOwnedStocksToFile()
  {
    Path filePath = Paths.get(directoryPath, OWNEDSTOCK_FILE);

    List<String> lines = new ArrayList<>();

    for (OwnedStock o : ownedStocks)
    {
      lines.add(toPSV(o));
    }
    try
    {
      Files.write(filePath, lines);
    }
    catch (IOException e)
    {
      throw new RuntimeException("Fejl ved skrivningen af ownedstocks: ", e);
    }
  }

  @Override public void rollback()
  {
    resetLists();
  }

  private void ensureFilesExist()
  {
    try
    {
      Path dir = Paths.get(directoryPath);

      if (!Files.exists(dir))
      {
        Files.createDirectories(dir);
      }
      createIfMissing(dir.resolve(PORTFOLIO_FILE));
      createIfMissing(dir.resolve(STOCK_FILE));
      createIfMissing(dir.resolve(OWNEDSTOCK_FILE));
    }
    catch (IOException e)
    {
      throw new RuntimeException(
          "Noget gik galt i persistence-files: " + directoryPath, e);
    }
  }

  private void createIfMissing(Path path) throws IOException
  {
    if (!Files.exists(path))
    {
      Files.createFile(path);
    }
  }

  public List<Portfolio> getPortfolios()
  {
    if (portfolios == null)
    {
      portfolios = loadPortfoliosFromFile();
    }
    return portfolios;
  }

  private List<Portfolio> loadPortfoliosFromFile()
  {
    List<Portfolio> list = new ArrayList<>();
    Path filePath = Paths.get(directoryPath, PORTFOLIO_FILE);
    try
    {
      List<String> lines = Files.readAllLines(filePath);
      for (String line : lines)
      {
        if (!line.isBlank())
        {
          list.add(portfolioFromPSV(line));
        }
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException("Fejl ved indlæsningen af portfolios ", e);
    }
    return list;
  }

  public List<Stock> getStocks()
  {
    if (stocks == null)
    {
      stocks = loadStocksFromFile();

    }
    return stocks;
  }

  private List<Stock> loadStocksFromFile()
  {
    List<Stock> list = new ArrayList<>();
    Path filePath = Paths.get(directoryPath, STOCK_FILE);
    try
    {
      List<String> lines = Files.readAllLines(filePath);

      for (String line : lines)
      {
        if (!line.isBlank())
        {
          list.add(stockFromPSV(line));
        }
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException("Fejl ved indlæsningen af stocks ", e);
    }
    return list;
  }

  public List<OwnedStock> getOwnedStocks()
  {
    if (ownedStocks == null)
    {
      ownedStocks = loadOwnedStocksFromFile();
    }
    return ownedStocks;
  }

  private List<OwnedStock> loadOwnedStocksFromFile()
  {
    List<OwnedStock> list = new ArrayList<>();
    Path filePath = Paths.get(directoryPath, OWNEDSTOCK_FILE);

    try
    {
      List<String> lines = Files.readAllLines(filePath);
      for (String line : lines)
      {
        if (!line.isBlank())
        {
          list.add(ownedStockFromPSV(line));
        }
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException("Fejl ved indlæsningen af ownedstocks", e);
    }
    return list;
  }

  private Portfolio portfolioFromPSV(String line)
  {
    String[] parts = line.split("\\|");
    if (parts.length < 2)
    {
      throw new RuntimeException("Ugyldig linje for portfolio: " + line);
    }

    UUID id = UUID.fromString(parts[0]);
    BigDecimal balance = new BigDecimal(parts[1]);
    return new Portfolio(id, balance);
  }

  private Stock stockFromPSV(String line)
  {
    String[] parts = line.split("\\|");

    if (parts.length < 4)
    {
      throw new RuntimeException("Ugyldig linje for stock: " + line);
    }
    String symbol = parts[0];
    String name = parts[1];
    BigDecimal currentPrice = new BigDecimal(parts[2]);
    String currentState = parts[3];
    return new Stock(symbol, name, currentPrice, currentState);
  }

  private OwnedStock ownedStockFromPSV(String line)
  {
    String[] parts = line.split("\\|");

    if (parts.length < 4)
    {
      throw new RuntimeException("Ugyldig linje for ownedstock " + line);
    }
    UUID ownedStockId = UUID.fromString(parts[0]);
    UUID portfolioId = UUID.fromString(parts[1]);
    String stockSymbol = parts[2];
    int numberOfShares = Integer.parseInt(parts[3]);

    return new OwnedStock(ownedStockId, portfolioId, stockSymbol,
                          numberOfShares);
  }

  private String toPSV(Portfolio p)
  {
    return p.getPortfolioId() + "|" + p.getCurrentBalance().toPlainString();
  }

  private String toPSV(Stock s)
  {
    return s.getSymbol() + "|" + s.getName() + "|" + s.getCurrentPrice()
                                                      .toPlainString() + "|"
        + s.getCurrentState();
  }

  private String toPSV(OwnedStock o)
  {
    return o.getOwnedStockId() + "|" + o.getPortfolioId() + "|"
        + o.getStockSymbol() + "|" + o.getNumberOfShares();
  }

  private void resetLists()
  {
    portfolios  = null;
    stocks      = null;
    ownedStocks = null;
  }

}
