package presentation.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import presentation.viewmodels.DashboardViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardHomeController implements Initializable
{
  @FXML private Label welcomeTitleLabel;

  private final DashboardViewModel viewModel;

  public DashboardHomeController(DashboardViewModel viewModel)
  {
    this.viewModel = viewModel;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    welcomeTitleLabel.textProperty().bind(viewModel.welcomeMessageProperty());
  }

  @FXML
  private void handleOpenPortfolio()
  {
    viewModel.openPortfolio();
  }

  @FXML
  private void handleViewMarket()
  {
    viewModel.openMarket();
  }
}