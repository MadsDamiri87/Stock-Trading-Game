package presentation.viewmodels;

import business.services.interfaces.StockPriceHistoryInterface;
import business.stockmarket.StockMarketUpdateEvent;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import presentation.core.NavigationService;
import presentation.listeners.StockUpdateReceiver;

import java.util.HashMap;
import java.util.Map;

public class StockMarketViewModel implements StockUpdateReceiver
{
  private static final int MAX_DATA_POINTS = 30;

  private final NavigationService navigationsService;
  private final StockPriceHistoryInterface stockPriceHistoryInterface;

  private final StringProperty marketStatus = new SimpleStringProperty("Live");
  private final StringProperty symbol = new SimpleStringProperty("-");
  private final StringProperty currentPrice = new SimpleStringProperty("-");

  private final ObservableList<XYChart.Series<Number, Number>> chartSeries = FXCollections.observableArrayList();

  private final Map<String, XYChart.Series<Number, Number>> seriesBySymbol = new HashMap<>();
  private final Map<String, Integer> tickCountBySymbol = new HashMap<>();

  private final IntegerProperty highestTick = new SimpleIntegerProperty(0);

  public StockMarketViewModel(NavigationService navigationService,
                              StockPriceHistoryInterface stockPriceHistoryInterface)
  {
    this.navigationsService = navigationService;
    this.stockPriceHistoryInterface = stockPriceHistoryInterface;
  }

  public void openPortfolio()
  {
    navigationsService.openPortfolio();
  }

  @Override public void onStockUpdateViewModel(StockMarketUpdateEvent event)
  {
    marketStatus.set(event.currentState());
    symbol.set(event.stockSymbol());
    currentPrice.set(String.format("%.2f", event.currentPrice()));

    String stockSymbol = event.stockSymbol();

    XYChart.Series<Number, Number> series = seriesBySymbol.get(stockSymbol);

    if (series == null)
    {
      series = new XYChart.Series<>();
      series.setName(stockSymbol);

      seriesBySymbol.put(stockSymbol, series);
      tickCountBySymbol.put(stockSymbol, 0);
      chartSeries.add(series);
    }

    int nextTick = tickCountBySymbol.get(stockSymbol) + 1;
    tickCountBySymbol.put(stockSymbol, nextTick);

    if (nextTick > highestTick.get())
    {
      highestTick.set(nextTick);
    }

    series.getData().add(new XYChart.Data<>(nextTick, event.currentPrice()));

    if (series.getData().size() > MAX_DATA_POINTS)
    {
      series.getData().remove(0);
    }
  }

  public StringProperty marketStatusProperty()
  {
    return marketStatus;
  }

  public StringProperty symbolProperty()
  {
    return symbol;
  }

  public StringProperty currentPriceProperty()
  {
    return currentPrice;
  }

  public ObservableList<XYChart.Series<Number, Number>> getChartSeries()
  {
    return chartSeries;
  }

  public IntegerProperty highestTickProperty()
  {
    return highestTick;
  }

  public int getMaxDataPoints()
  {
    return MAX_DATA_POINTS;
  }
}