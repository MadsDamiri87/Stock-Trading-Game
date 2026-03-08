package persistence.fileimplementation;

import entities.*;
import persistence.interfaces.UnitOfWork;
import shared.logging.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUnitOfWork implements UnitOfWork

{
  private final String directoryPath;

  private List<Portfolio> portfolios;
  private List<Stock> stocks;
  private List<OwnedStock> ownedStocks;
  private List<Transaction> transactions;
  private List<StockPriceHistory> stockPriceHistories;
  private final Logger logger = Logger.getInstance();

  private static final String PORTFOLIO_FILE = "portfolios.psv";
  private static final String STOCK_FILE = "stocks.psv";
  private static final String OWNEDSTOCK_FILE = "ownedstocks.psv";
  private static final String TRANSACTION_FILE = "transactions.psv";
  private static final String STOCK_PRICE_HISTORY_FILE = "stock_price_history.psv";

  public FileUnitOfWork(String directoryPath)
  {
    this.directoryPath = directoryPath;
    ensureFilesExist();
  }

  @Override public void beginTransaction()
  {
    logger.log("Info", "Transaction started");
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
    if (transactions != null)
    {
      writeTransactionsToFile();
    }

    if (stockPriceHistories != null)
    {
      writeStockPriceHistoriesToFile();
    }

    resetLists();
  }

  @Override public void rollback()
  {
    logger.log("INFO", "Transaction rolled back");
    resetLists();
  }

  private void writePortfoliosToFile()
  {
    List<String> lines = new ArrayList<>();

    for (Portfolio portfolio : portfolios)
    {
      lines.add(stockPriceHistoryToPSV(portfolio));
    }

    writeLinesToFile(PORTFOLIO_FILE, lines, "Portfolio blev skrevet til fil",
                     "Fejl i writePortfoliosToFile");
  }

  private void writeStocksToFile()
  {
    List<String> lines = new ArrayList<>();

    for (Stock stock : stocks)
    {
      lines.add(stockPriceHistoryToPSV(stock));
    }

    writeLinesToFile(STOCK_FILE, lines, "Stock blev skrevet til fil",
                     "Fejl i writeStocksToFile");
  }

  private void writeOwnedStocksToFile()
  {
    List<String> lines = new ArrayList<>();

    for (OwnedStock ownedStock : ownedStocks)
    {
      lines.add(stockPriceHistoryToPSV(ownedStock));
    }

    writeLinesToFile(OWNEDSTOCK_FILE, lines, "OwnedStock blev skrevet til fil",
                     "Fejl i writeOwnedStocksToFile");
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
      createIfMissing(dir.resolve(TRANSACTION_FILE));
      createIfMissing(dir.resolve(STOCK_PRICE_HISTORY_FILE));
    }
    catch (IOException e)
    {
      logger.log("Error", "Fejl i ensureFilesExist: " + e.getMessage());
      throw new RuntimeException(
          "Noget gik galt i persistence-files: " + directoryPath, e);
    }
  }

  private void createIfMissing(Path path) throws IOException
  {
    if (!Files.exists(path))
    {
      logger.log("Info", "File at: " + path
          + " wasn't found. New file was created in createIfMissing");
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
    List<Portfolio> portfolios = new ArrayList<>();
    List<String> lines = readLinesFromFile(PORTFOLIO_FILE,
                                           "Fejl ved indlæsningen af portfolios");
    for (String line : lines)
    {
      if (!line.isBlank())
      {
        portfolios.add(portfolioFromPSV(line));
      }
    }
    return portfolios;
  }

  private List<Stock> loadStocksFromFile()
  {
    List<Stock> stocks = new ArrayList<>();

    List<String> lines = readLinesFromFile(STOCK_FILE,
                                           "Fejl ved indlæsningen af stocks");

    for (String line : lines)
    {
      if (!line.isBlank())
      {
        stocks.add(stockFromPSV(line));
      }
    }

    return stocks;
  }

  public List<Stock> getStocks()
  {
    if (stocks == null)
    {
      stocks = loadStocksFromFile();
    }
    return stocks;
  }

  public List<OwnedStock> getOwnedStocks()
  {
    if (ownedStocks == null)
    {
      ownedStocks = loadOwnedStocksFromFile();
      logger.log("Info", "Ownedstocks blev indlæst fra fil");
    }
    return ownedStocks;
  }

  private List<OwnedStock> loadOwnedStocksFromFile()
  {
    List<OwnedStock> ownedStocks = new ArrayList<>();

    List<String> lines = readLinesFromFile(OWNEDSTOCK_FILE,
                                           "Fejl ved indlæsningen af ownedstocks");

    for (String line : lines)
    {
      if (!line.isBlank())
      {
        ownedStocks.add(ownedStockFromPSV(line));
      }
    }
    return ownedStocks;
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

  private String stockPriceHistoryToPSV(Portfolio p)
  {
    return p.getPortfolioId() + " | " + p.getCurrentBalance().toPlainString();
  }

  private String stockPriceHistoryToPSV(Stock s)
  {
    return s.getSymbol() + " | " + s.getName() + " | " + s.getCurrentPrice()
                                                      .toPlainString() + " | "
        + s.getCurrentState();
  }

  private String stockPriceHistoryToPSV(OwnedStock o)
  {
    return o.getOwnedStockId() + " | " + o.getPortfolioId() + " | "
        + o.getStockSymbol() + " | " + o.getNumberOfShares();
  }

  private void resetLists()
  {
    portfolios          = null;
    stocks              = null;
    ownedStocks         = null;
    transactions        = null;
    stockPriceHistories = null;
  }

  private void writeLinesToFile(String fileName, List<String> lines,
                                String successMessage, String errorMessage)
  {
    Path filePath = Paths.get(directoryPath, fileName);

    try
    {
      Files.write(filePath, lines);
      logger.log("Info", successMessage + " " + filePath);
    }
    catch (IOException e)
    {
      logger.log("Error", errorMessage + " " + e.getMessage());
      throw new RuntimeException(errorMessage, e);
    }
  }

  private List<String> readLinesFromFile(String fileName, String errorMessage)
  {
    Path filePath = Paths.get(directoryPath, fileName);

    try
    {
      return Files.readAllLines(filePath);
    }
    catch (IOException e)
    {
      logger.log("Error", errorMessage + " " + e.getMessage());
      throw new RuntimeException(errorMessage, e);
    }
  }

  public List<Transaction> getTransactions()
  {
    if (transactions == null)
    {
      transactions = loadTransactionsFromFile();
    }
    return transactions;
  }

  private String stockPriceHistoryToPSV(Transaction transaction)
  {
    return transaction.transactionId() + " | " + transaction.portfolioId() + " | "
        + transaction.stockSymbol() + " | " + transaction.type() + " | "
        + transaction.quantity() + " | " + transaction.pricePerShare() + " | "
        + transaction.totalAmount() + " | " + transaction.fee() + " | "
        + transaction.timestamp();
  }

  private List<Transaction> loadTransactionsFromFile()

  {
    List<Transaction> transactions = new ArrayList<>();

    List<String> lines = readLinesFromFile(TRANSACTION_FILE,
                                           "Fejl ved indlæsningen af transaktioner");

    for (String line : lines)
    {
      if (!line.isBlank())
      {
        transactions.add(transactionFromPSV(line));
      }
    }

    return transactions;
  }

  private Transaction transactionFromPSV(String line)
  {
    String[] parts = line.split("\\|");

    if (parts.length < 9)
    {
      throw new RuntimeException("Ugyldig linje for transaction " + line);
    }

    return new Transaction(UUID.fromString(parts[0]), UUID.fromString(parts[1]),
                           parts[2], parts[3], Integer.parseInt(parts[4]),
                           new BigDecimal(parts[5]), new BigDecimal(parts[6]),
                           new BigDecimal(parts[7]), Instant.parse(parts[8]));
  }

  private void writeTransactionsToFile()
  {
    List<String> lines = new ArrayList<>();

    for (Transaction transaction : transactions)
    {
      lines.add(stockPriceHistoryToPSV(transaction));
    }

    writeLinesToFile(TRANSACTION_FILE, lines,
                     "Transactions blev skrevet til fil",
                     "Fejl i writeTransactionsToFile");
  }

  private void writeStockPriceHistoriesToFile()
  {
    List<String> lines = new ArrayList<>();

    for (StockPriceHistory history : stockPriceHistories)
    {
      lines.add(stockPriceHistoryToPSV(history));
    }

    writeLinesToFile(STOCK_PRICE_HISTORY_FILE, lines,
                     "StockPriceHistories blev skrevet til fil",
                     "Fejl i writeStockPriceHistoriesToFile");
  }

  public List<StockPriceHistory> getStockPriceHistories()
  {
    if (stockPriceHistories == null)
    {
      stockPriceHistories = loadStockPriceHistoriesFromFile();
    }
    return stockPriceHistories;
  }

  private List<StockPriceHistory> loadStockPriceHistoriesFromFile()
  {
    List<StockPriceHistory> histories = new ArrayList<>();

    List<String> lines = readLinesFromFile(STOCK_PRICE_HISTORY_FILE,
                                           "Fejl ved indlæsningen af stock price histories");

    for (String line : lines)
    {
      if (!line.isBlank())
      {
        histories.add(stockPriceHistoryFromPSV(line));
      }
    }

    return histories;
  }

  private String stockPriceHistoryToPSV(StockPriceHistory history)
  {
    return history.getStockPriceHistId() + " | " + history.getStockSymbolId()
        + " | " + history.getPrice() + " | " + history.getTimestamp();
  }

  private StockPriceHistory stockPriceHistoryFromPSV(String line)
  {
    String[] parts = line.split("\\|");

    if (parts.length < 4)
    {
      throw new RuntimeException(
          "Ugyldig linje for StockPriceHistory: " + line);
    }

    return new StockPriceHistory(UUID.fromString(parts[0]), parts[1],
                                 new BigDecimal(parts[2]),
                                 Instant.parse(parts[3]));
  }
  // TODO
  //  Forenkel udgaven af hvordan der skrives og loades fra filer med generics.
  //  se filmen om det på:
  //  https://www.youtube.com/watch?v=K1iu1kXkVoA
  //  https://www.youtube.com/watch?v=FXAUXvPNKi8
  //  https://www.youtube.com/watch?si=2xVbYeAgRmYb0nPw&v=vqjA6dqugq8&feature=youtu.be
  //
  //  private <T> List<T> loadData(String filePath, Function<String, T> mapper)
  //  {
  //    List<String> lines = readAllLines(filePath);
  //    List<T> result = new ArrayList<>();
  //    for (String line : lines)
  //    {
  //      if (!line.trim().isEmpty())
  //      {
  //        result.add(mapper.apply(line));
  //      }
  //    }
  //    return result;
  //  }
  //
  //  private List<Transaction> loadTransactionsFromFile() {
  //    return loadData(getTransactionFilePath(), this::transactionFromPSV);
  //  }

}
