package presentation.core;

import presentation.viewmodels.BuyStocksViewModel;
import presentation.viewmodels.DashboardViewModel;
import presentation.viewmodels.PopUpWelcomeViewModel;
import presentation.viewmodels.PortfolioViewModel;
import presentation.viewmodels.SellStocksViewModel;
import presentation.viewmodels.StockMarketViewModel;

public class ApplicationContext
{
  private final DashboardViewModel dashboardViewModel;
  private final PopUpWelcomeViewModel popUpWelcomeViewModel;
  private final PortfolioViewModel portfolioViewModel;
  private final StockMarketViewModel stockMarketViewModel;
  private final BuyStocksViewModel buyStocksViewModel;
  private final SellStocksViewModel sellStocksViewModel;

  public ApplicationContext()
  {
    dashboardViewModel = new DashboardViewModel();
    popUpWelcomeViewModel = new PopUpWelcomeViewModel(dashboardViewModel);
    portfolioViewModel = new PortfolioViewModel(dashboardViewModel);
    stockMarketViewModel = new StockMarketViewModel(dashboardViewModel);
    buyStocksViewModel = new BuyStocksViewModel();
    sellStocksViewModel = new SellStocksViewModel();
  }

  public PopUpWelcomeViewModel getPopUpWelcomeViewModel()
  {
    return popUpWelcomeViewModel;
  }

  public DashboardViewModel getDashboardViewModel()
  {
    return dashboardViewModel;
  }

  public PortfolioViewModel getPortfolioViewModel()
  {
    return portfolioViewModel;
  }

  public StockMarketViewModel getStockMarketViewModel()
  {
    return stockMarketViewModel;
  }

  public BuyStocksViewModel getBuyStocksViewModel()
  {
    return buyStocksViewModel;
  }

  public SellStocksViewModel getSellStocksViewModel()
  {
    return sellStocksViewModel;
  }
}