package presentation.viewmodels;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import presentation.core.NavigationService;
import presentation.core.ViewManager;

public class PopUpWelcomeViewModel
{
  private final StringProperty traderName = new SimpleStringProperty("");
  private final NavigationService navigationsService;

  public PopUpWelcomeViewModel(NavigationService navigationsService)
  {
    this.navigationsService = navigationsService;
  }

  public void continueToDashboard()
  {
    navigationsService.setTraderName(traderName.get());
    ViewManager.openMainApplication("DashboardView");
    navigationsService.openDashboardHome();
  }

  public StringProperty traderNameProperty()
  {
    return traderName;
  }
}