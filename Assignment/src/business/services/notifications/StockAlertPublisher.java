package business.services.notifications;

public interface StockAlertPublisher
{
  void publish(StockAlert alert);
}
