package presentation.controllers;

import business.dto.OwnedStockDTO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import presentation.viewmodels.SellStocksViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public class SellStocksController implements Initializable
{
  @FXML private Label statusDescription;
  @FXML private Label tradePrice;
  @FXML private Label balanceLabel;
  @FXML private Label stockName;
  @FXML private Label ownedSharesLabel;
  @FXML private LineChart<Number, Number> stockChart;
  @FXML private NumberAxis xAxis;
  @FXML private NumberAxis yAxis;

  @FXML private Label pricePerShare;
  @FXML private Label selectedStock;
  @FXML private ListView<OwnedStockDTO> stockListView;
  @FXML private TextField sharesField;

  @FXML private Label summaryPriceLabel;
  @FXML private Label summaryTotalLabel;
  @FXML private Label statusLabel;

  private final SellStocksViewModel viewModel;

  public SellStocksController(SellStocksViewModel viewModel)
  {
    this.viewModel = viewModel;
  }

  @Override public void initialize(URL location, ResourceBundle resources)
  {
    viewModel.loadBalance();
    viewModel.loadStocks();

    statusDescription.textProperty().bind(viewModel.statusDescription());
    tradePrice.textProperty().bind(viewModel.tradePriceProperty());
    selectedStock.textProperty().bind(viewModel.symbolProperty());
    stockName.textProperty().bind(viewModel.stockNameProperty());
    ownedSharesLabel.textProperty().bind(viewModel.ownedSharesProperty());
    sharesField.textProperty().bindBidirectional(viewModel.sharesProperty());
    pricePerShare.textProperty().bind(viewModel.priceProperty());
    balanceLabel.textProperty().bind(viewModel.balanceProperty());

    summaryPriceLabel.textProperty().bind(viewModel.summaryPriceProperty());
    summaryTotalLabel.textProperty().bind(viewModel.summaryTotalProperty());
    statusLabel.textProperty().bind(viewModel.statusMessageProperty());

    stockListView.setItems(viewModel.getOwnedStocks());

    stockListView.setCellFactory(listView -> new ListCell<>()
    {
      @Override protected void updateItem(OwnedStockDTO item, boolean empty)
      {
        super.updateItem(item, empty);

        if (empty || item == null)
        {
          setText(null);
        }
        else
        {
          setText(item.stockSymbol() + " - " + item.numberOfShares() + " shares");
        }
      }
    });

    stockListView.getSelectionModel().selectedItemProperty()
                 .addListener((obs, oldValue, newValue) -> {
                   viewModel.selectOwnedStock(newValue);
                 });

    stockChart.setAnimated(false);
    stockChart.setCreateSymbols(false);
    stockChart.getData().clear();
    stockChart.getData().add(viewModel.getSelectedStockSeries());

    xAxis.setAutoRanging(false);
    xAxis.setLowerBound(1);
    xAxis.setUpperBound(viewModel.getMaxDataPoints());
    xAxis.setTickUnit(5);

    yAxis.setAutoRanging(true);
    yAxis.setForceZeroInRange(false);
  }

  @FXML private void handleEstimate()
  {
    viewModel.estimate();
  }

  @FXML private void handleSell()
  {
    viewModel.sell();
    sharesField.clear();
  }
}