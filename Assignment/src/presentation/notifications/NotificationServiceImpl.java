package presentation.notifications;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class NotificationServiceImpl implements NotificationService
{
  private final ObservableList<NotificationMessage> notifications = FXCollections.observableArrayList();

  @Override public void notify(String type, String message)
  {
    Runnable addTask = () -> notifications.add(new NotificationMessage(type, message));

    if (Platform.isFxApplicationThread())
    {
      addTask.run();
    }
    else
    {
      Platform.runLater(addTask);
    }
  }

  @Override public ObservableList<NotificationMessage> getNotifications()
  {
    return notifications;
  }

  @Override public void remove(NotificationMessage notification)
  {
    Runnable removeTask = () -> notifications.remove(notification);

    if (Platform.isFxApplicationThread())
    {
      removeTask.run();
    }
    else
    {
      Platform.runLater(removeTask);
    }
  }
}