package presentation.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import presentation.core.NavigationService;
import presentation.viewmodels.DashboardViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardHomeController implements Initializable
{
  @FXML private Label welcomeTitleLabel;

  private final NavigationService navigationService;
  private final DashboardViewModel viewModel;

  public DashboardHomeController(NavigationService navigationService, DashboardViewModel viewModel)
  {
    this.navigationService = navigationService;
    this.viewModel         = viewModel;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    welcomeTitleLabel.textProperty().bind(viewModel.welcomeMessageProperty());
  }

  @FXML
  private void handleOpenPortfolio()
  {
    navigationService.openPortfolio();
  }

  @FXML
  private void handleViewMarket()
  {
    navigationService.openMarketView();
  }
}