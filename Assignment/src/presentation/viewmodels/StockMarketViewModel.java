package presentation.viewmodels;

public class StockMarketViewModel
{

  private DashboardViewModel viewModel;


  public StockMarketViewModel(DashboardViewModel viewModel)
  {
    this.viewModel = viewModel;

  }
  public void openPortfolio()
  {
    viewModel.openPortfolio();
  }



}
