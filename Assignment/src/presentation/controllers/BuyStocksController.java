package presentation.controllers;

import business.dto.StockDTO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import presentation.viewmodels.BuyStocksViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public class BuyStocksController implements Initializable
{
  @FXML private Label selectedStock;
  @FXML private TextField sharesField;
  @FXML private Label pricePerShare;

  @FXML private ListView<StockDTO> stockListView;
  @FXML private Label balanceLabel;
  @FXML private LineChart<Number, Number> stockChart;
  @FXML private NumberAxis xAxis;
  @FXML private NumberAxis yAxis;

  @FXML private Label stockName;
  @FXML private Label ownedSharesLabel;
  @FXML private Label summaryPriceLabel;
  @FXML private Label summaryTotalLabel;
  @FXML private Label statusLabel;

  private final BuyStocksViewModel viewModel;

  public BuyStocksController(BuyStocksViewModel viewModel)
  {
    this.viewModel = viewModel;
  }

  @Override public void initialize(URL location, ResourceBundle resources)
  {
    refresh();

    selectedStock.textProperty().bind(viewModel.symbolProperty());
    sharesField.textProperty().bindBidirectional(viewModel.sharesProperty());
    pricePerShare.textProperty().bind(viewModel.priceProperty());
    balanceLabel.textProperty().bind(viewModel.balanceProperty());

    stockName.textProperty().bind(viewModel.stockNameProperty());
    ownedSharesLabel.textProperty().bindBidirectional(viewModel.summarySharesProperty());
    summaryPriceLabel.textProperty().bind(viewModel.summaryPriceProperty());
    summaryTotalLabel.textProperty().bind(viewModel.summaryTotalProperty());
    statusLabel.textProperty().bind(viewModel.statusMessageProperty());

    stockListView.setItems(viewModel.getAvailableStocks());

    stockListView.setCellFactory(listView -> new ListCell<>()
    {
      @Override protected void updateItem(StockDTO item, boolean empty)
      {
        super.updateItem(item, empty);

        if (empty || item == null)
        {
          setText(null);
        }
        else
        {
          setText(item.symbol() + " - " + item.name());
        }
      }
    });

    stockListView.getSelectionModel().selectedItemProperty()
                 .addListener((obs, oldValue, newValue) -> {
                   viewModel.selectStock(newValue);
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

  private void refresh()
  {
    viewModel.loadBalance();
    viewModel.loadStocks();
  }

  @FXML private void handleEstimate()
  {
    viewModel.estimate();
  }

  @FXML private void handleBuy()
  {
    viewModel.buy();
    sharesField.clear();
  }
}