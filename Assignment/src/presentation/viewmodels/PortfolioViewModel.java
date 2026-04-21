package presentation.viewmodels;

import business.dto.OwnedStockDTO;
import business.dto.PortfolioDTO;
import business.dto.TransactionDTO;
import business.services.interfaces.PortfolioServiceInterface;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import presentation.core.NavigationService;
import presentation.state.UserSession;
import shared.logging.Logger;

import java.util.UUID;

public class PortfolioViewModel
{
  private final NavigationService navigationService;
  private final PortfolioServiceInterface portfolioService;
  private final UserSession userSession;
  private final Logger logger = Logger.getInstance();

  private final StringProperty balance = new SimpleStringProperty("-");
  private final BooleanProperty canTrade = new SimpleBooleanProperty(false);

  private final ObservableList<OwnedStockDTO> ownedStocks = FXCollections.observableArrayList();
  private final ObservableList<TransactionDTO> transactions = FXCollections.observableArrayList();

  public PortfolioViewModel(NavigationService navigationService,
                            PortfolioServiceInterface portfolioService, UserSession userSession)
  {
    this.navigationService        = navigationService;
    this.portfolioService = portfolioService;
    this.userSession      = userSession;
  }

  public void loadPortfolio()
  {
    try
    {
      UUID portfolioId = userSession.getActivePortfolioId();

      if (portfolioId == null)
      {
        balance.set("No Portfolio");
        canTrade.set(false);
        return;
      }

      PortfolioDTO portfolio = portfolioService.getPortfolio(portfolioId);

      balance.set(portfolio.currentBalance().toPlainString());
      ownedStocks.setAll(portfolio.ownedStocks());
      transactions.setAll(portfolioService.getTransactionHistory(portfolioId, 0, 50));

      canTrade.set(true);
      logger.log("Info", "Portfolio loaded for id: " + portfolioId);
    }
    catch (Exception e)
    {
      logger.log("Error", "Failed to load portfolio: " + e.getMessage());
      balance.set("Error");
      canTrade.set(false);
      ownedStocks.clear();
      transactions.clear();
    }
  }

  public void sellStocks()
  {
    navigationService.sellStocks();
  }

  public void openBuyStocks()
  {
    navigationService.buyStocks();
  }

  public StringProperty balanceProperty()
  {
    return balance;
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