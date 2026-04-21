package presentation.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import presentation.viewmodels.StockMarketViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public class StockMarketController implements Initializable
{
  @FXML private Label marketStatusLabel;
  @FXML private Label symbolLabel;
  @FXML private Label currentPriceLabel;

  @FXML private LineChart<Number, Number> priceChart;
  @FXML private NumberAxis xAxis;
  @FXML private NumberAxis yAxis;

  @FXML private VBox marketOverviewBox;

  private final StockMarketViewModel viewModel;

  public StockMarketController(StockMarketViewModel viewModel)
  {
    this.viewModel = viewModel;
  }

  @Override public void initialize(URL location, ResourceBundle resources)
  {
    marketStatusLabel.textProperty().bind(viewModel.marketStatusProperty());
    symbolLabel.textProperty().bind(viewModel.symbolProperty());
    currentPriceLabel.textProperty().bind(viewModel.currentPriceProperty());

    priceChart.setAnimated(false);
    priceChart.setCreateSymbols(false);
    priceChart.setData(viewModel.getChartSeries());

    xAxis.setAutoRanging(false);
    xAxis.setLowerBound(1);
    xAxis.setUpperBound(viewModel.getMaxDataPoints());
    xAxis.setTickUnit(5);

    yAxis.setAutoRanging(true);
    yAxis.setForceZeroInRange(false);

    viewModel.highestTickProperty().addListener((obs, oldValue, newValue) -> {
      updateXAxisWindow(newValue.intValue());
    });
  }

  private void updateXAxisWindow(int highestTick)
  {
    int windowSize = viewModel.getMaxDataPoints();

    if (highestTick <= windowSize)
    {
      xAxis.setLowerBound(1);
      xAxis.setUpperBound(windowSize);
    }
    else
    {
      xAxis.setLowerBound(highestTick - windowSize + 1);
      xAxis.setUpperBound(highestTick);
    }
  }

  @FXML private void handleOpenPortfolio()
  {
    viewModel.openPortfolio();
  }
}