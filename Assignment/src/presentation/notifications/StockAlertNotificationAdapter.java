package presentation.notifications;

import business.services.notifications.StockAlert;
import business.services.notifications.StockAlertPublisher;

public class StockAlertNotificationAdapter implements StockAlertPublisher
{
  private final NotificationService notificationService;

  public StockAlertNotificationAdapter(NotificationService notificationService)
  {
    this.notificationService = notificationService;
  }

  @Override public void publish(StockAlert alert)
  {
    notificationService.notify(alert.type().name(), alert.message());
  }
}
