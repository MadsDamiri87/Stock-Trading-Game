package presentation.notifications;

import javafx.collections.ObservableList;

public interface NotificationService

{
  void notify(String type, String message);
  ObservableList<NotificationMessage> getNotifications();
  void remove(NotificationMessage notification);
}
