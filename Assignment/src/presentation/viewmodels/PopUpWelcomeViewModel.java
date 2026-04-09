package presentation.viewmodels;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import presentation.core.ViewManager;

public class PopUpWelcomeViewModel
{
  private final StringProperty traderName = new SimpleStringProperty("");
  private final DashboardViewModel dashboardViewModel;

  public PopUpWelcomeViewModel(DashboardViewModel dashboardViewModel)
  {
    this.dashboardViewModel = dashboardViewModel;
  }

  public void continueToDashboard()
  {
    dashboardViewModel.setTraderName(traderName.get());
    ViewManager.openMainApplication("DashboardView");
    dashboardViewModel.openDashboardHome();
  }

  public StringProperty traderNameProperty()
  {
    return traderName;
  }
}