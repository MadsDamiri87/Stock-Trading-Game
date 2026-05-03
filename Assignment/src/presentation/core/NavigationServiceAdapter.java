package presentation.core;

import presentation.viewmodels.DashboardViewModel;

public class NavigationServiceAdapter implements NavigationService
{
  private final DashboardViewModel dashboardViewModel;

  public NavigationServiceAdapter(DashboardViewModel dashboardViewModel)
  {
    this.dashboardViewModel = dashboardViewModel;
  }

  @Override public void openDashboardHome()
  {
    navigateTo("dashboard", "DashboardHomeView");
  }

  @Override public void openPortfolio()
  {
    navigateTo("portfolio", "PortfolioView");
  }

  @Override public void openBuyStocksView()
  {
    navigateTo("buy", "BuyStocksView");
  }

  @Override public void openSellStocksView()
  {
    navigateTo("sell", "SellStocksView");
  }

  @Override public void openMarketView()
  {
    navigateTo("market", "StockMarketView");
  }

  @Override public void setTraderName(String traderName)
  {
    String safeName = traderName == null ? "" : traderName.trim();

    if (safeName.isBlank())
    {
      dashboardViewModel.welcomeMessageProperty().set("Welcome back, Trader");
    }
    else
    {
      dashboardViewModel.welcomeMessageProperty().set("Welcome back, " + safeName);
    }
  }

  private void navigateTo(String activeView, String fxmlView)
  {
    dashboardViewModel.activeViewProperty().set(activeView);
    ViewManager.showView(fxmlView);
  }
}
