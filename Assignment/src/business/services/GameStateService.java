package business.services;

import business.services.interfaces.GameStateServiceInterface;
import business.stockmarket.StockMarket;
import entities.Stock;
import persistence.interfaces.StockDAO;
import shared.logging.Logger;

import java.math.BigDecimal;
import java.util.List;

public class GameStateService implements GameStateServiceInterface
{
  private final StockMarket stockMarket;
  private final StockSetupService stockSetupService;
  private final StockDAO stockDAO;
  private final Logger logger = Logger.getInstance();

  private boolean marketInitialized;
  private boolean gameRunning;

  public GameStateService(StockMarket stockMarket, StockSetupService stockSetupService,
                          StockDAO stockDAO, boolean marketInitialized, boolean gameRunning)
  {
    this.stockMarket       = stockMarket;
    this.stockSetupService = stockSetupService;
    this.stockDAO          = stockDAO;
    this.marketInitialized = marketInitialized;
    this.gameRunning       = gameRunning;
  }

  @Override public void startGame()
  {
    if (!marketInitialized)
    {
      initializeMarket();
    }

    gameRunning = true;
    logger.log("Info", "Game started");
  }

  @Override public void stopGame()
  {
    gameRunning = false;
    logger.log("Info", "Game stopped");
  }

  @Override public boolean isGameRunning()
  {
    return gameRunning;
  }

  @Override public boolean isMarketInitialized()
  {
    return marketInitialized;
  }

  public void initializeMarket()
  {
    if (marketInitialized)
    {
      logger.log("Info", "Market is already initialized");
      return;
    }
    logger.log("Info", "Initializing stock market");

    List<Stock> existingStocks = stockDAO.getAll();

    if (existingStocks.isEmpty())
    {
      logger.log("Info", "No stocks found in persistence. Creating default stocks.");

      addStockToGame("AAPL", "Apple", BigDecimal.valueOf(150), "Steady");
      addStockToGame("TSLA", "Tesla", BigDecimal.valueOf(180), "Steady");
      addStockToGame("NVDA", "Nvidia", BigDecimal.valueOf(200), "Steady");
      addStockToGame("MSFT", "Microsoft", BigDecimal.valueOf(320), "Steady");
      addStockToGame("GOOGL", "Alphabet", BigDecimal.valueOf(140), "Steady");
      addStockToGame("AMZN", "Amazon", BigDecimal.valueOf(135), "Steady");
      addStockToGame("META", "Meta", BigDecimal.valueOf(300), "Steady");
      addStockToGame("GM", "General Motors", BigDecimal.valueOf(210), "Steady");

    }
    else
    {
      logger.log("Info", "Loading existing stocks into stock market");

      for (Stock stock : existingStocks)
      {
        stockMarket.addExistingStock(stock);
      }
    }
    marketInitialized = true;
    logger.log("Info", "Stock market initialized");
  }

  private void addStockToGame(String symbol, String name, BigDecimal price, String state)
  {
    Stock stock = stockSetupService.getOrCreateStock(symbol, name, price, state);
    stockMarket.addExistingStock(stock);

    logger.log("Info", "Stock added to market and persistence: " + symbol);
  }

  public void resetGame()
  {
    gameRunning       = false;
    marketInitialized = false;
    logger.log("Info", "Game state was reset - NO data was reset");
  }

  public void updateMarket()
  {
    logger.log("Info", "Updating stock market");
    stockMarket.updateAllStocks();
  }
}