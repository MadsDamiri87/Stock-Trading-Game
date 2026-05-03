package business.services;

import business.services.notifications.StockAlert;
import business.services.notifications.StockAlertPublisher;
import business.services.notifications.StockAlertType;
import business.stockmarket.StockMarketListener;
import business.stockmarket.StockMarketUpdateEvent;
import persistence.interfaces.OwnedStockDAO;
import shared.logging.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StockAlertService implements StockMarketListener
{
  private final Logger logger;
  private OwnedStockDAO ownedStockDAO;
  private StockAlertPublisher alertPublisher;
  private UUID portfolioId;

  private final Set<String> risingAlerted = new HashSet<>();
  private final Set<String> nearBankruptAlerted = new HashSet<>();
  private final Set<String> bankruptAlerted = new HashSet<>();

  public StockAlertService()
  {
    this.logger = Logger.getInstance();
  }

  public StockAlertService(OwnedStockDAO ownedStockDAO, StockAlertPublisher alertPublisher,
                           UUID portfolioId)
  {
    this.logger         = Logger.getInstance();
    this.ownedStockDAO  = ownedStockDAO;
    this.alertPublisher = alertPublisher;
    this.portfolioId    = portfolioId;
  }

  @Override public void onStockUpdated(StockMarketUpdateEvent event)
  {
    boolean isOwned = ownedStockDAO.getByPortfolioIdAndStockSymbol(portfolioId, event.stockSymbol())
                                   .isPresent();

    if (!isOwned)
    {
      return;
    }

    String symbol = event.stockSymbol();
    double price = event.currentPrice();
    String state = event.currentState();

    if (state.equalsIgnoreCase("Bankrupt"))
    {
      if (!bankruptAlerted.contains(symbol))
      {
        logger.log("Alert", "Stock: " + symbol + " er Bankrupt");
        alertPublisher.publish(
            new StockAlert(StockAlertType.BANKRUPT, symbol, symbol + " has gone bankrupt."));
        bankruptAlerted.add(symbol);
      }
      return;
    }
    if (state.equalsIgnoreCase("Reset"))
    {
      logger.log("Alert", "Stock: " + symbol + " er blevet reset og kan købes igen");
    }

    if (price > 180)
    {
      if (!risingAlerted.contains(symbol))
      {
        logger.log("Alert", "Stock: " + symbol + " er meget høj: " + price);
        alertPublisher.publish(
            new StockAlert(StockAlertType.RISING, symbol, symbol + " is rising rapidly."));
        risingAlerted.add(symbol);
      }
    }
    else
    {
      risingAlerted.remove(symbol);
    }

    if (price < 50)
    {
      if (!nearBankruptAlerted.contains(symbol))
      {
        logger.log("Alert", "Stock " + symbol + " er meget lav: " + price);
        alertPublisher.publish(new StockAlert(StockAlertType.NEAR_BANKRUPTCY, symbol,
                                              symbol + " is near bankruptcy."));
        nearBankruptAlerted.add(symbol);
      }
    }
    else
    {
      nearBankruptAlerted.remove(symbol);
    }

  }
}

