package presentation.viewmodels;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PortfolioViewModel
{
  private final StringProperty balance = new SimpleStringProperty();
  private final BooleanProperty canTrade = new SimpleBooleanProperty(false);

  private DashboardViewModel viewModel;

  public PortfolioViewModel(DashboardViewModel viewModel)
  {
    this.viewModel = viewModel;
  }

  public void sellStocks()
  {
   viewModel.sellStocks();
  }

  public void openBuyStocks()
  {
    viewModel.buyStocks();
  }
}