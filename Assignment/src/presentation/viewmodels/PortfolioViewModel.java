package presentation.viewmodels;

import business.dto.OwnedStockDTO;
import business.dto.PortfolioDTO;
import business.dto.TransactionDTO;
import business.services.interfaces.PortfolioServiceInterface;
import business.stockmarket.StockMarketUpdateEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import presentation.core.NavigationService;
import presentation.listeners.StockUpdateReceiver;
import presentation.state.UserSession;
import shared.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortfolioViewModel implements StockUpdateReceiver
{
  private final NavigationService navigationService;
  private final PortfolioServiceInterface portfolioService;
  private final UserSession userSession;
  private final Logger logger = Logger.getInstance();

  private final StringProperty balance = new SimpleStringProperty("-");
  private final StringProperty totalValue = new SimpleStringProperty("-");
  private final StringProperty openPositions = new SimpleStringProperty("-");
  private final StringProperty holdingCount = new SimpleStringProperty("-");
  private final StringProperty holdingsRefreshTrigger = new SimpleStringProperty("");

  private final BooleanProperty canTrade = new SimpleBooleanProperty(false);

  private final ObservableList<OwnedStockDTO> ownedStocks =
      FXCollections.observableArrayList();

  private final ObservableList<TransactionDTO> transactions =
      FXCollections.observableArrayList();

  private final Map<String, Double> currentPrices = new HashMap<>();

  private PortfolioDTO currentPortfolio;

  public PortfolioViewModel(NavigationService navigationService,
                            PortfolioServiceInterface portfolioService,
                            UserSession userSession)
  {
    this.navigationService = navigationService;
    this.portfolioService = portfolioService;
    this.userSession = userSession;

    ownedStocks.addListener(
        (javafx.collections.ListChangeListener<OwnedStockDTO>) change ->
            updatePortfolioSummary()
    );
  }

  public void loadPortfolio()
  {
    try
    {
      UUID portfolioId = userSession.getActivePortfolioId();

      if (portfolioId == null)
      {
        reset();
        return;
      }

      currentPortfolio = portfolioService.getPortfolio(portfolioId);

      ownedStocks.setAll(currentPortfolio.ownedStocks());

      transactions.setAll(
          portfolioService.getTransactionHistory(portfolioId, 0, 50)
      );

      updatePortfolioSummary();

      canTrade.set(true);

      holdingsRefreshTrigger.set(String.valueOf(System.nanoTime()));

      logger.log("Info", "Portfolio loaded for id: " + portfolioId);
    }
    catch (Exception e)
    {
      logger.log("Error", "Failed to load portfolio: " + e.getMessage());
      reset();
    }
  }

  @Override
  public void onStockUpdate(StockMarketUpdateEvent event)
  {
    currentPrices.put(event.stockSymbol(), event.currentPrice());

    updatePortfolioSummary();

    holdingsRefreshTrigger.set(String.valueOf(System.nanoTime()));
  }

  private void updatePortfolioSummary()
  {
    int totalShares = ownedStocks.stream()
                                 .mapToInt(OwnedStockDTO::numberOfShares)
                                 .sum();

    double holdingsValue = ownedStocks.stream()
                                      .mapToDouble(stock ->
                                                       getCurrentPriceFor(stock) * stock.numberOfShares()
                                      )
                                      .sum();

    double cash = 0.0;

    if (currentPortfolio != null)
    {
      cash = currentPortfolio.currentBalance().doubleValue();
    }

    balance.set(formatMoney(cash));
    totalValue.set(formatMoney(cash + holdingsValue));
    openPositions.set(String.valueOf(totalShares));
    holdingCount.set("Across " + ownedStocks.size() + " holdings");
  }

  private String formatMoney(double value)
  {
    return String.format("%.2f", value);
  }

  private void reset()
  {
    currentPortfolio = null;

    balance.set("-");
    totalValue.set("-");
    openPositions.set("-");
    holdingCount.set("-");
    holdingsRefreshTrigger.set("");

    canTrade.set(false);

    ownedStocks.clear();
    transactions.clear();
    currentPrices.clear();
  }

  public double getCurrentPriceFor(OwnedStockDTO stock)
  {
    return currentPrices.getOrDefault(
        stock.stockSymbol(),
        stock.lastBuyPrice()
    );
  }

  public double getProfitLossFor(OwnedStockDTO stock)
  {
    double current = getCurrentPriceFor(stock);
    return (current - stock.lastBuyPrice()) * stock.numberOfShares();
  }

  public void navigateToBuyStocksView()
  {
    navigationService.openBuyStocksView();
  }

  public void navigateToSellStocksView()
  {
    navigationService.openSellStocksView();
  }

  public StringProperty balanceProperty()
  {
    return balance;
  }

  public ObservableValue<String> totalValueProperty()
  {
    return totalValue;
  }

  public ObservableValue<String> openPositionProperty()
  {
    return openPositions;
  }

  public ObservableValue<String> holdingsCountProperty()
  {
    return holdingCount;
  }

  public StringProperty holdingsRefreshTriggerProperty()
  {
    return holdingsRefreshTrigger;
  }

  public BooleanProperty canTradeProperty()
  {
    return canTrade;
  }

  public ObservableList<OwnedStockDTO> getOwnedStocks()
  {
    return ownedStocks;
  }

  public ObservableList<TransactionDTO> getTransactions()
  {
    return transactions;
  }
}