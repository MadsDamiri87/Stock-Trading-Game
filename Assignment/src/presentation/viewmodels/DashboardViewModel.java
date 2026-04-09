package presentation.viewmodels;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import presentation.core.NavigationService;
import presentation.core.ViewManager;

public class DashboardViewModel implements NavigationService
{
  private final StringProperty welcomeMessage =
      new SimpleStringProperty("Welcome back, Trader");

  private final StringProperty activeView =
      new SimpleStringProperty("dashboard");

  private final BooleanProperty sidebarExpanded =
      new SimpleBooleanProperty(true);

  private final StringProperty sidebarTitle =
      new SimpleStringProperty("Stock Trading");
  private final StringProperty sidebarSubtitle =
      new SimpleStringProperty("Main Menu");

  private final StringProperty dashboardButtonText =
      new SimpleStringProperty("Dashboard");
  private final StringProperty portfolioButtonText =
      new SimpleStringProperty("Portfolio");
  private final StringProperty buyButtonText =
      new SimpleStringProperty("Buy Stocks");
  private final StringProperty sellButtonText =
      new SimpleStringProperty("Sell Stocks");
  private final StringProperty marketButtonText =
      new SimpleStringProperty("Market");
  private final StringProperty settingsButtonText =
      new SimpleStringProperty("Settings");
  private final StringProperty closeAppButtonText =
      new SimpleStringProperty("Close Application");

  private static final double EXPANDED_WIDTH = 240;
  private static final double COLLAPSED_WIDTH = 86;

  public void setTraderName(String traderName)
  {
    String safeName = traderName == null ? "" : traderName.trim();

    if (safeName.isBlank())
    {
      welcomeMessage.set("Welcome back, Trader");
    }
    else
    {
      welcomeMessage.set("Welcome back, " + safeName);
    }
  }

  public void toggleSidebar()
  {
    sidebarExpanded.set(!sidebarExpanded.get());
    updateSidebarTexts();
  }

  private void updateSidebarTexts()
  {
    if (sidebarExpanded.get())
    {
      sidebarTitle.set("Stock Trading");
      sidebarSubtitle.set("Main Menu");

      dashboardButtonText.set("Dashboard");
      portfolioButtonText.set("Portfolio");
      buyButtonText.set("Buy Stocks");
      sellButtonText.set("Sell Stocks");
      marketButtonText.set("Market");
      settingsButtonText.set("Settings");
      closeAppButtonText.set("Close Application");
    }
    else
    {
      sidebarTitle.set("");
      sidebarSubtitle.set("");

      dashboardButtonText.set("⌂");
      portfolioButtonText.set("▣");
      buyButtonText.set("⊕");
      sellButtonText.set("⊖");
      marketButtonText.set("◉");
      settingsButtonText.set("⚙");
      closeAppButtonText.set("⏻");
    }
  }

  public double getTargetSidebarWidth()
  {
    return sidebarExpanded.get() ? EXPANDED_WIDTH : COLLAPSED_WIDTH;
  }

  public boolean isSidebarExpanded()
  {
    return sidebarExpanded.get();
  }

  public BooleanProperty sidebarExpandedProperty()
  {
    return sidebarExpanded;
  }

  public void openDashboardHome()
  {
    activeView.set("dashboard");
    ViewManager.showView("DashboardHomeView");
  }

  public void openPortfolio()
  {
    activeView.set("portfolio");
    ViewManager.showView("PortfolioView");
  }

  public void buyStocks()
  {
    activeView.set("buy");
    ViewManager.showView("BuyStocksView");
  }

  public void sellStocks()
  {
    activeView.set("sell");
    ViewManager.showView("SellStocksView");
  }

  public void openMarket()
  {
    activeView.set("market");
    ViewManager.showView("StockMarketView");
  }

  public StringProperty welcomeMessageProperty()
  {
    return welcomeMessage;
  }

  public StringProperty activeViewProperty()
  {
    return activeView;
  }

  public StringProperty sidebarTitleProperty()
  {
    return sidebarTitle;
  }

  public StringProperty sidebarSubtitleProperty()
  {
    return sidebarSubtitle;
  }

  public StringProperty dashboardButtonTextProperty()
  {
    return dashboardButtonText;
  }

  public StringProperty portfolioButtonTextProperty()
  {
    return portfolioButtonText;
  }

  public StringProperty buyButtonTextProperty()
  {
    return buyButtonText;
  }

  public StringProperty sellButtonTextProperty()
  {
    return sellButtonText;
  }

  public StringProperty marketButtonTextProperty()
  {
    return marketButtonText;
  }

  public StringProperty settingsButtonTextProperty()
  {
    return settingsButtonText;
  }

  public StringProperty closeAppButtonTextProperty()
  {
    return closeAppButtonText;
  }


}