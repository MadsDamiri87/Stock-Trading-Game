package presentation.viewmodels;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SellStocksViewModel
{
  private final StringProperty symbol = new SimpleStringProperty("");
  private final StringProperty shares = new SimpleStringProperty("");
  private final StringProperty price = new SimpleStringProperty("");

  private final StringProperty summarySymbol = new SimpleStringProperty("-");
  private final StringProperty summaryShares = new SimpleStringProperty("-");
  private final StringProperty summaryPrice = new SimpleStringProperty("-");
  private final StringProperty summaryTotal = new SimpleStringProperty("-");
  private final StringProperty statusMessage =
      new SimpleStringProperty("Fill in the form to prepare your sell order.");

  public void estimate()
  {
    try
    {
      String cleanSymbol = symbol.get() == null ? "" : symbol.get().trim().toUpperCase();
      int parsedShares = Integer.parseInt(shares.get().trim());
      double parsedPrice = Double.parseDouble(price.get().trim());
      double total = parsedShares * parsedPrice;

      summarySymbol.set(cleanSymbol.isBlank() ? "-" : cleanSymbol);
      summaryShares.set(String.valueOf(parsedShares));
      summaryPrice.set(String.format("$%.2f", parsedPrice));
      summaryTotal.set(String.format("$%.2f", total));
      statusMessage.set("Estimated sell order is ready.");
    }
    catch (Exception e)
    {
      statusMessage.set("Enter a valid symbol, share count, and price.");
    }
  }

  public void sell()
  {
    estimate();
    if (!summaryTotal.get().equals("-"))
    {
      statusMessage.set("Sell order prepared for " + summarySymbol.get() + ".");
    }
  }

  public StringProperty symbolProperty() { return symbol; }
  public StringProperty sharesProperty() { return shares; }
  public StringProperty priceProperty() { return price; }

  public StringProperty summarySymbolProperty() { return summarySymbol; }
  public StringProperty summarySharesProperty() { return summaryShares; }
  public StringProperty summaryPriceProperty() { return summaryPrice; }
  public StringProperty summaryTotalProperty() { return summaryTotal; }
  public StringProperty statusMessageProperty() { return statusMessage; }
}