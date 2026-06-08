package presentation.notifications;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import provided.CustomAlertBox;

public class CustomAlertBoxAdapter implements NotificationService
{
  private final CustomAlertBox alertBox = new CustomAlertBox();
  private final ObservableList<NotificationMessage> notifications =
      FXCollections.observableArrayList();

  @Override
  public void notify(String type, String message)
  {
    Runnable task = () -> {
      notifications.add(new NotificationMessage(type, message));

      CustomAlertBox.AlertType alertType = switch (type.toUpperCase())
      {
        case "ERROR" -> CustomAlertBox.AlertType.ERROR;
        case "WARNING", "WARN" -> CustomAlertBox.AlertType.WARNING;
        default -> CustomAlertBox.AlertType.INFO;
      };

      alertBox.showAlert(message, type, alertType);
    };

    if (Platform.isFxApplicationThread())
    {
      task.run();
    }
    else
    {
      Platform.runLater(task);
    }
  }

  @Override
  public ObservableList<NotificationMessage> getNotifications()
  {
    return notifications;
  }

  @Override
  public void remove(NotificationMessage notification)
  {
    notifications.remove(notification);
  }
}