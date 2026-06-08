package presentation.viewmodels;

import business.dto.*;
import business.services.interfaces.PortfolioServiceInterface;
import business.services.interfaces.StockPriceHistoryInterface;
import business.services.interfaces.TradingServiceInterface;
import business.stockmarket.StockMarketUpdateEvent;
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

public class SellStocksViewModel implements StockUpdateReceiver
{
  private static final int MAX_DATA_POINTS = 30;

  private final TradingServiceInterface tradingService;
  private final PortfolioServiceInterface portfolioService;
  private final StockPriceHistoryInterface stockHistoryService;
  private final UserSession userSession;
  private final Logger logger = Logger.getInstance();

  private final UUID portfolioId;

  private final ObservableList<OwnedStockDTO> ownedStocks = FXCollections.observableArrayList();
  private final ObjectProperty<OwnedStockDTO> selectedOwnedStock = new SimpleObjectProperty<>();

  private final StringProperty symbol = new SimpleStringProperty("-");
  private final StringProperty stockName = new SimpleStringProperty("-");
  private final StringProperty shares = new SimpleStringProperty("");
  private final StringProperty price = new SimpleStringProperty("-");
  private final StringProperty balance = new SimpleStringProperty("-");
  private final StringProperty ownedShares = new SimpleStringProperty("0");
  private final StringProperty tradePrice = new SimpleStringProperty("-");
  private final StringProperty statusDescription = new SimpleStringProperty("-");

  private final StringProperty summaryShares = new SimpleStringProperty("-");
  private final StringProperty summaryPrice = new SimpleStringProperty("-");
  private final StringProperty summaryTotal = new SimpleStringProperty("-");
  private final StringProperty statusMessage = new SimpleStringProperty(
      "Choose an owned stock and enter shares.");

  private final XYChart.Series<Number, Number> selectedStockSeries = new XYChart.Series<>();

  private int tickCounter = 0;
  private double currentPriceValue = 0.0;

  public SellStocksViewModel(TradingServiceInterface tradingService,
                             PortfolioServiceInterface portfolioService,
                             StockPriceHistoryInterface stockPriceHistoryInterface, UserSession userSession)
  {
    this.tradingService             = tradingService;
    this.portfolioService    = portfolioService;
    this.stockHistoryService = stockPriceHistoryInterface;
    this.userSession         = userSession;
    this.portfolioId                = userSession.getActivePortfolioId();

    selectedStockSeries.setName("Selected Stock");

    loadPortfolioData();
  }

  public void loadPortfolioData()
  {
    try
    {
      if (portfolioId == null)
      {
        balance.set("No active portfolio");
        ownedStocks.clear();
        return;
      }

      PortfolioDTO portfolio = portfolioService.getPortfolio(portfolioId);

      balance.set(String.format("$%.2f", portfolio.currentBalance()));
      ownedStocks.setAll(portfolio.ownedStocks());
    }
    catch (Exception e)
    {
      logger.log("Error",
                 "Failed to load portfolio data in SellStocksViewModel: " + e.getMessage());

      balance.set("Error");
      ownedStocks.clear();
    }
  }

  public void selectOwnedStock(OwnedStockDTO ownedStock)
  {
    selectedOwnedStock.set(ownedStock);

    if (ownedStock == null)
    {
      symbol.set("-");
      stockName.set("-");
      price.set("-");
      ownedShares.set("0");
      summaryPrice.set("-");
      tradePrice.set("-");
      statusDescription.set("-");
      loadHistoryForSelectedStock(ownedStock.stockSymbol());
      currentPriceValue = 0.0;
      tickCounter = 0;
      return;
    }

    symbol.set(ownedStock.stockSymbol());
    stockName.set(ownedStock.stockSymbol());
    ownedShares.set(String.valueOf(ownedStock.numberOfShares()));
    tradePrice.set(String.format("$%.2f", ownedStock.lastBuyPrice()));

    StockDTO matchingStock = portfolioService.getAvailableStocks().stream().filter(
                                                 stock -> stock.symbol().equalsIgnoreCase(ownedStock.stockSymbol())).findFirst()
                                             .orElse(null);

    if (matchingStock != null)
    {
      currentPriceValue = matchingStock.currentPrice().doubleValue();
      price.set(String.format("$%.2f", currentPriceValue));
      summaryPrice.set(String.format("$%.2f", currentPriceValue));
    }
    else
    {
      currentPriceValue = 0.0;
      price.set("-");
      summaryPrice.set("-");
    }

    selectedStockSeries.getData().clear();
    tickCounter = 0;

    estimate();
  }

  @Override public void onStockUpdateViewModel(StockMarketUpdateEvent event)
  {
    OwnedStockDTO currentSelection = selectedOwnedStock.get();

    if (currentSelection == null)
    {
      return;
    }

    if (!currentSelection.stockSymbol().equalsIgnoreCase(event.stockSymbol()))
    {
      return;
    }

    currentPriceValue = event.currentPrice();

    price.set(String.format("$%.2f", currentPriceValue));
    summaryPrice.set(String.format("$%.2f", currentPriceValue));

    tickCounter++;
    selectedStockSeries.getData().add(new XYChart.Data<>(tickCounter, currentPriceValue));

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
      OwnedStockDTO currentSelection = selectedOwnedStock.get();

      if (currentSelection == null)
      {
        statusMessage.set("Choose an owned stock first.");
        return;
      }

      int parsedShares = Integer.parseInt(shares.get().trim());
      double total = parsedShares * currentPriceValue;

      summaryShares.set(String.valueOf(parsedShares));
      summaryPrice.set(String.format("$%.2f", currentPriceValue));
      summaryTotal.set(String.format("$%.2f", total));
      statusMessage.set("Estimated sell order is ready.");
    }
    catch (NumberFormatException e)
    {
      statusMessage.set("Enter a valid share count.");
    }
    catch (Exception e)
    {
      statusMessage.set("Could not estimate sell order.");
    }
  }

  public void sell()
  {
    try
    {
      OwnedStockDTO currentSelection = selectedOwnedStock.get();

      if (portfolioId == null)
      {
        statusMessage.set("No active portfolio found.");
        return;
      }

      if (currentSelection == null)
      {
        statusMessage.set("Choose an owned stock first.");
        return;
      }

      int parsedShares = Integer.parseInt(shares.get().trim());

      TradeRequestDTO request = new TradeRequestDTO(portfolioId, currentSelection.stockSymbol(),
                                                    parsedShares, currentPriceValue);

      tradingService.sellStock(request);

      logger.log("Info", "Sell completed for " + currentSelection.stockSymbol() + ", shares: "
          + parsedShares);

      statusMessage.set(
          "Sold " + parsedShares + " shares of " + currentSelection.stockSymbol() + ".");

      loadPortfolioData();

      OwnedStockDTO updatedSelection = ownedStocks.stream().filter(
                                                      stock -> stock.stockSymbol().equalsIgnoreCase(currentSelection.stockSymbol())).findFirst()
                                                  .orElse(null);

      selectOwnedStock(updatedSelection);
    }
    catch (NumberFormatException e)
    {
      logger.log("Error", "Invalid share count in SellStocksViewModel: " + e.getMessage());
      statusMessage.set("Enter a valid share count.");
    }
    catch (Exception e)
    {
      logger.log("Error", "Sell failed in SellStocksViewModel: " + e.getMessage());
      statusMessage.set(e.getMessage());
    }
  }

  public void loadStocks()
  {
    loadPortfolioData();
  }

  public void loadBalance()
  {
    try
    {
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
      logger.log("Error", "Failed to load balance in SellStocksViewModel: " + e.getMessage());
      balance.set("Error");
    }
  }

  private void loadHistoryForSelectedStock(String symbol)
  {
    selectedStockSeries.getData().clear();

    List<StockPriceHistoryDTO> history = stockHistoryService.getHistoryForStock(symbol);

    int index = 1;

    for (StockPriceHistoryDTO point : history)
    {
      selectedStockSeries.getData().add(
          new XYChart.Data<>(index, point.price().doubleValue())
      );

      index++;
    }

    tickCounter = history.size();
  }

  public ObservableList<OwnedStockDTO> getOwnedStocks()
  {
    return ownedStocks;
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

  public StringProperty stockNameProperty()
  {
    return stockName;
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

  public StringProperty ownedSharesProperty()
  {
    return ownedShares;
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

  public StringProperty tradePriceProperty()
  {
    return tradePrice;
  }

  public StringProperty statusDescription()
  {
    return statusDescription;
  }
}