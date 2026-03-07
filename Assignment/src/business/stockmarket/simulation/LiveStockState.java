package business.stockmarket.simulation;

public interface LiveStockState
{

  double calculatePriceXhange(LiveStock liveStock);
  String getStateName();
}
