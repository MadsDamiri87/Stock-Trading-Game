package presentation.controllers;

import javafx.fxml.Initializable;
import presentation.viewmodels.PortfolioViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public class PortfolioController implements Initializable
{
  private final PortfolioViewModel viewModel;

  public PortfolioController(PortfolioViewModel viewModel)
  {
    this.viewModel = viewModel;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
  }

  public void openBuyStocksFromPortfolio()
  {
    viewModel.openBuyStocks();
  }

  public void openSellStocksFromPortfolio()
  {
    viewModel.sellStocks();
  }
}