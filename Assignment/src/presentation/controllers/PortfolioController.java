package presentation.controllers;

import business.dto.OwnedStockDTO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import presentation.viewmodels.PortfolioViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public class PortfolioController implements Initializable
{
  @FXML private VBox holdingsBox;
  @FXML private Label openPositionLabel;
  @FXML private Label holdingCountLabel;
  @FXML private Label cashBalanceLabel;
  @FXML private Label todayChangeLabel;
  @FXML private Label totalValueLabel;

  private final PortfolioViewModel viewModel;

  public PortfolioController(PortfolioViewModel viewModel)
  {
    this.viewModel = viewModel;
  }

  @Override public void initialize(URL location, ResourceBundle resources)
  {
    cashBalanceLabel.textProperty().bind(viewModel.balanceProperty());
    totalValueLabel.textProperty().bind(viewModel.totalValueProperty());
    openPositionLabel.textProperty().bind(viewModel.openPositionProperty());
    holdingCountLabel.textProperty().bind(viewModel.holdingsCountProperty());

    viewModel.getOwnedStocks().addListener((javafx.collections.ListChangeListener<OwnedStockDTO>) change -> refreshHoldings());

    viewModel.holdingsRefreshTriggerProperty().addListener(
        (obs, oldValue, newValue) -> refreshHoldings()
    );

    viewModel.loadPortfolio();
    refreshHoldings();
  }

  private void refreshHoldings()
  {
    while (holdingsBox.getChildren().size() > 1)
    {
      holdingsBox.getChildren().remove(1);
    }

    for (OwnedStockDTO stock : viewModel.getOwnedStocks())
    {
      GridPane row = new GridPane();
      row.setHgap(25);
      addColumnConstraints(row);

      Label symbol = new Label(stock.stockSymbol());
      symbol.getStyleClass().add("table-cell-strong");

      Label shares = new Label(String.valueOf(stock.numberOfShares()));
      shares.getStyleClass().add("table-cell");

      Label avgPrice = new Label(String.format("%.2f", stock.lastBuyPrice()));
      avgPrice.getStyleClass().add("table-cell");

      double currentPriceValue = viewModel.getCurrentPriceFor(stock);
      Label current = new Label(String.format("%.2f", currentPriceValue));
      current.getStyleClass().add("table-cell");

      double profitLossValue = viewModel.getProfitLossFor(stock);
      Label profitLoss = new Label(String.format("%.2f", profitLossValue));
      profitLoss.getStyleClass().add(
          profitLossValue >= 0 ? "table-positive" : "table-negative"
      );

      row.add(symbol, 0, 0);
      row.add(shares, 1, 0);
      row.add(avgPrice, 2, 0);
      row.add(current, 3, 0);
      row.add(profitLoss, 4, 0);

      holdingsBox.getChildren().add(row);
    }
  }

  private void addColumnConstraints(GridPane grid)
  {
    grid.getColumnConstraints().addAll(
        new ColumnConstraints(140),
        new ColumnConstraints(90),
        new ColumnConstraints(130),
        new ColumnConstraints(130),
        new ColumnConstraints(130)
    );
  }

  public void openBuyStocksFromPortfolio()
  {
    viewModel.navigateToBuyStocksView();
  }

  public void openSellStocksFromPortfolio()
  {
    viewModel.navigateToSellStocksView();
  }
}