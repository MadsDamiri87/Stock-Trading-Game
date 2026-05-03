package presentation.notifications;

import javafx.collections.ObservableList;
import provided.CustomAlertBox;

public class CustomAlertBoxAdapter implements NotificationService
{
  private final CustomAlertBox alertBox = new CustomAlertBox();


  @Override

  public void notify(String type, String message)
  {
    CustomAlertBox.AlertType alertType;

    switch (type.toUpperCase())
    {
      case "ERROR" -> alertType = CustomAlertBox.AlertType.ERROR;
      case "WARNING", "WARN" -> alertType = CustomAlertBox.AlertType.WARNING;
      default -> alertType = CustomAlertBox.AlertType.INFO;
    }

    alertBox.showAlert(message, type, alertType);
  }

  @Override public ObservableList<NotificationMessage> getNotifications()
  {
    return javafx.collections.FXCollections.observableArrayList();
  }

  @Override public void remove(NotificationMessage notification)
  {
  }
}
