package presentation.viewmodels;

import business.dto.*;
import business.services.interfaces.StockPriceHistoryInterface;
import business.stockmarket.StockMarketUpdateEvent;
import business.services.interfaces.PortfolioServiceInterface;
import business.services.interfaces.TradingServiceInterface;
import business.strategies.fee.FeeStrategySelector;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import presentation.listeners.StockUpdateReceiver;
import presentation.state.UserSession;
import shared.logging.Logger;

import java.util.List;
import java.util.UUID;

public class BuyStocksViewModel implements StockUpdateReceiver
{
  private static final int MAX_DATA_POINTS = 30;

  private final TradingServiceInterface tradingService;
  private final PortfolioServiceInterface portfolioService;
  private final StockPriceHistoryInterface stockPriceHistoryInterface;
  private final UserSession userSession;
  private final FeeStrategySelector feeStrategySelector;
  private final Logger logger = Logger.getInstance();

  private final String[] feeStrategies = {"Percentage", "Flat", "Volume"};
  private int currentFeeStrategyIndex = 0;

  private final ObservableList<StockDTO> availableStocks = FXCollections.observableArrayList();
  private final ObjectProperty<StockDTO> selectedStock = new SimpleObjectProperty<>();

  private final StringProperty symbol = new SimpleStringProperty("-");
  private final StringProperty shares = new SimpleStringProperty("");
  private final StringProperty price = new SimpleStringProperty("-");
  private final StringProperty balance = new SimpleStringProperty("-");
  private final StringProperty tradePrice = new SimpleStringProperty("-");

  private final StringProperty stockName = new SimpleStringProperty("-");
  private final StringProperty summaryShares = new SimpleStringProperty("-");
  private final StringProperty summaryPrice = new SimpleStringProperty("-");
  private final StringProperty summaryTotal = new SimpleStringProperty("-");
  private final StringProperty statusMessage = new SimpleStringProperty(
      "Choose a stock and enter shares.");
  private final StringProperty feeType = new SimpleStringProperty("-");

  private final XYChart.Series<Number, Number> selectedStockSeries = new XYChart.Series<>();
  private int tickCounter = 0;

  public BuyStocksViewModel(TradingServiceInterface tradingService,
                            PortfolioServiceInterface portfolioService,
                            StockPriceHistoryInterface stockPriceHistoryInterface,
                            UserSession userSession, FeeStrategySelector feeStrategySelector)
  {
    this.tradingService             = tradingService;
    this.portfolioService           = portfolioService;
    this.stockPriceHistoryInterface = stockPriceHistoryInterface;
    this.userSession                = userSession;
    this.feeStrategySelector        = feeStrategySelector;

    selectedStockSeries.setName("Selected Stock");

    loadStocks();
    loadBalance();
  }

  public void changeFeeStrategy()
  {
    currentFeeStrategyIndex = (currentFeeStrategyIndex + 1) % feeStrategies.length;

    String selectedStrategy = feeStrategies[currentFeeStrategyIndex];

    feeStrategySelector.selectStrategy(selectedStrategy);
    feeType.set(selectedStrategy);
    statusMessage.set("Fee strategy changed to: " + selectedStrategy);
  }

  public void loadStocks()
  {
    availableStocks.setAll(portfolioService.getAvailableStocks());
  }

  public void loadBalance()
  {
    try
    {
      UUID portfolioId = userSession.getActivePortfolioId();

      if (portfolioId == null)
      {
        balance.set("No active portfolio");
        return;
      }

      PortfolioDTO portfolio = portfolioService.getPortfolio(portfolioId);
      balance.set(String.format("$%.2f", portfolio.currentBalance()));
    }
    catch (Exception e)
    {
      logger.log("Error", "Failed to load balance in BuyStocksViewModel: " + e.getMessage());
      balance.set("Error");
    }
  }

  private void loadHistoryForSelectedStock(String symbol)
  {
    selectedStockSeries.getData().clear();

    List<StockPriceHistoryDTO> history = stockPriceHistoryInterface.getHistoryForStock(symbol);

    int index = 1;

    for (StockPriceHistoryDTO point : history)
    {
      selectedStockSeries.getData().add(new XYChart.Data<>(index, point.price().doubleValue()));

      index++;
    }
    tickCounter = history.size();
  }

  public void selectStock(StockDTO stock)
  {
    selectedStock.set(stock);

    if (stock == null)
    {
      symbol.set("-");
      price.set("-");
      stockName.set("-");
      summaryPrice.set("-");
      loadOwnedSharesForSelectedStock();
      return;
    }

    symbol.set(stock.symbol());
    price.set(String.format("$%.2f", stock.currentPrice()));
    stockName.set(stock.name());
    summaryPrice.set(String.format("$%.2f", stock.currentPrice()));

    loadLastBuyPriceForSelectedStock();
    loadOwnedSharesForSelectedStock();

    selectedStockSeries.getData().clear();
    tickCounter = 0;

    estimate();
  }

  @Override public void onStockUpdateViewModel(StockMarketUpdateEvent event)
  {
    StockDTO currentSelection = selectedStock.get();

    if (currentSelection == null)
    {
      return;
    }

    if (!currentSelection.symbol().equalsIgnoreCase(event.stockSymbol()))
    {
      return;
    }

    price.set(String.format("$%.2f", event.currentPrice()));
    summaryPrice.set(String.format("$%.2f", event.currentPrice()));

    tickCounter++;
    selectedStockSeries.getData().add(new XYChart.Data<>(tickCounter, event.currentPrice()));

    if (selectedStockSeries.getData().size() > MAX_DATA_POINTS)
    {
      selectedStockSeries.getData().remove(0);
    }

    estimate();
  }

  public void estimate()
  {
    try
    {
      StockDTO stock = selectedStock.get();

      if (stock == null)
      {
        statusMessage.set("Choose a stock first.");
        return;
      }

      int parsedShares = Integer.parseInt(shares.get().trim());
      double parsedPrice = parsePriceValue(price.get());
      double total = parsedShares * parsedPrice;

      stockName.set(stock.name());
      summaryShares.set(String.valueOf(parsedShares));
      summaryPrice.set(String.format("$%.2f", parsedPrice));
      summaryTotal.set(String.format("$%.2f", total));
      statusMessage.set("Estimated buy order is ready.");
    }
    catch (NumberFormatException e)
    {
      statusMessage.set("Enter a valid share count.");
    }
    catch (Exception e)
    {
      statusMessage.set("Could not estimate order.");
    }
  }

  public void buy()
  {
    try
    {
      StockDTO stock = selectedStock.get();
      UUID portfolioId = userSession.getActivePortfolioId();

      if (portfolioId == null)
      {
        statusMessage.set("No active portfolio found.");
        return;
      }

      if (stock == null)
      {
        statusMessage.set("Choose a stock first.");
        return;
      }

      int parsedShares = Integer.parseInt(shares.get().trim());

      TradeRequestDTO request = new TradeRequestDTO(portfolioId, stock.symbol(), parsedShares,
                                                    stock.currentPrice().doubleValue());
      tradingService.buyStock(request);

      logger.log("Info", "Buy completed for " + stock.symbol() + ", shares: " + parsedShares);
      statusMessage.set("Buy order completed for " + stock.symbol() + ".");

      loadBalance();
      loadOwnedSharesForSelectedStock();
      loadLastBuyPriceForSelectedStock();
      estimate();
    }
    catch (NumberFormatException e)
    {
      logger.log("Error", "Invalid share count in BuyStocksViewModel: " + e.getMessage());
      statusMessage.set("Enter a valid share count.");
    }
    catch (Exception e)
    {
      logger.log("Error", "Buy failed in BuyStocksViewModel: " + e.getMessage());
      statusMessage.set(e.getMessage());
    }
  }

  private void loadOwnedSharesForSelectedStock()
  {
    try
    {
      UUID portfolioId = userSession.getActivePortfolioId();

      if (portfolioId == null)
      {
        summaryShares.set("0");
        return;
      }
      String currentSymbol = symbol.get();

      if (currentSymbol == null || currentSymbol.isBlank() || currentSymbol.equals("-"))
      {
        summaryShares.set("0");
        return;
      }

      PortfolioDTO portfolio = portfolioService.getPortfolio(portfolioId);

      int sharesOwned = portfolio.ownedStocks().stream().filter(
                                     stock -> stock.stockSymbol().equalsIgnoreCase(currentSymbol))
                                 .mapToInt(stock -> stock.numberOfShares()).findFirst().orElse(0);

      summaryShares.set(String.valueOf(sharesOwned));
    }
    catch (Exception e)
    {
      logger.log("Error", "Failed to load owned shares in BuyStocksViewModel: " + e.getMessage());
      summaryShares.set("0");
    }
  }

  private void loadLastBuyPriceForSelectedStock()
  {
    try
    {
      UUID portfolioId = userSession.getActivePortfolioId();

      if (portfolioId == null)
      {
        tradePrice.set("-");
        return;
      }

      String currentSymbol = symbol.get();

      if (currentSymbol == null || currentSymbol.isBlank() || currentSymbol.equals("-"))
      {
        tradePrice.set("-");
        return;
      }

      PortfolioDTO portfolio = portfolioService.getPortfolio(portfolioId);

      OwnedStockDTO owned = portfolio.ownedStocks().stream().filter(
          stock -> stock.stockSymbol().equalsIgnoreCase(currentSymbol)).findFirst().orElse(null);

      if (owned != null)
      {
        tradePrice.set(String.format("$%.2f", owned.lastBuyPrice()));
      }
      else
      {
        tradePrice.set("-");
      }
    }
    catch (Exception e)
    {
      logger.log("Error", "Failed to load buy price in BuyStocksViewModel: " + e.getMessage());
      tradePrice.set("-");
    }
  }

  private double parsePriceValue(String formattedPrice)
  {
    return Double.parseDouble(formattedPrice.replace("$", "").trim());
  }

  public ObservableList<StockDTO> getAvailableStocks()
  {
    return availableStocks;
  }

  public XYChart.Series<Number, Number> getSelectedStockSeries()
  {
    return selectedStockSeries;
  }

  public int getMaxDataPoints()
  {
    return MAX_DATA_POINTS;
  }

  public StringProperty symbolProperty()
  {
    return symbol;
  }

  public StringProperty sharesProperty()
  {
    return shares;
  }

  public StringProperty priceProperty()
  {
    return price;
  }

  public StringProperty balanceProperty()
  {
    return balance;
  }

  public StringProperty stockNameProperty()
  {
    return stockName;
  }

  public StringProperty summarySharesProperty()
  {
    return summaryShares;
  }

  public StringProperty summaryPriceProperty()
  {
    return summaryPrice;
  }

  public StringProperty summaryTotalProperty()
  {
    return summaryTotal;
  }

  public StringProperty statusMessageProperty()
  {
    return statusMessage;
  }

  public StringProperty feeTypeProperty()
  {
    return feeType;
  }

}