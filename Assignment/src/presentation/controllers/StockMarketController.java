package presentation.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import presentation.viewmodels.StockMarketViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public class StockMarketController implements Initializable
{

  private final StockMarketViewModel viewModel;

  public StockMarketController(StockMarketViewModel viewModel)
  {
    this.viewModel = viewModel;
  }

  @Override public void initialize(URL location, ResourceBundle resources)
  {
  }

  public void handleOpenPortfolio(ActionEvent actionEvent)
  {
    viewModel.openPortfolio();
  }

}
