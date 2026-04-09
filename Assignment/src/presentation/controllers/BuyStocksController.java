package presentation.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import presentation.viewmodels.BuyStocksViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public class BuyStocksController implements Initializable
{
  @FXML private TextField symbolField;
  @FXML private TextField sharesField;
  @FXML private TextField priceField;

  @FXML private Label summarySymbolLabel;
  @FXML private Label summarySharesLabel;
  @FXML private Label summaryPriceLabel;
  @FXML private Label summaryTotalLabel;
  @FXML private Label statusLabel;

  private final BuyStocksViewModel viewModel;

  public BuyStocksController(BuyStocksViewModel viewModel)
  {
    this.viewModel = viewModel;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    symbolField.textProperty().bindBidirectional(viewModel.symbolProperty());
    sharesField.textProperty().bindBidirectional(viewModel.sharesProperty());
    priceField.textProperty().bindBidirectional(viewModel.priceProperty());

    summarySymbolLabel.textProperty().bind(viewModel.summarySymbolProperty());
    summarySharesLabel.textProperty().bind(viewModel.summarySharesProperty());
    summaryPriceLabel.textProperty().bind(viewModel.summaryPriceProperty());
    summaryTotalLabel.textProperty().bind(viewModel.summaryTotalProperty());
    statusLabel.textProperty().bind(viewModel.statusMessageProperty());
  }

  @FXML
  private void handleEstimate()
  {
    viewModel.estimate();
  }

  @FXML
  private void handleBuy()
  {
    viewModel.buy();
  }
}