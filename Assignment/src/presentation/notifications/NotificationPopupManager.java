package presentation.notifications;

import javafx.animation.PauseTransition;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NotificationPopupManager
{
  private final NotificationService notificationService;
  private final Stage ownerStage;

  public NotificationPopupManager(NotificationService notificationService, Stage ownerStage)
  {
    this.notificationService = notificationService;
    this.ownerStage          = ownerStage;
  }

  public void start()
  {
    notificationService.getNotifications()
                       .addListener((ListChangeListener<NotificationMessage>) change -> {
                         while (change.next())
                         {
                           if (change.wasAdded())
                           {
                             for (NotificationMessage notification : change.getAddedSubList())
                             {
                               showPopup(notification);
                             }
                           }
                         }
                       });
  }

  private void showPopup(NotificationMessage notification)
  {
    Popup popup = new Popup();
    popup.setAutoFix(true);
    popup.setAutoHide(false);

    VBox root = new VBox(6);
    root.setPadding(new Insets(12));
    root.setMaxWidth(400);
    root.setMouseTransparent(true);

    root.setStyle("""
                          -fx-background-color: rgba(15, 23, 42, 0.96);
                          -fx-background-radius: 14;
                          -fx-border-radius: 14;
                          -fx-border-color: rgba(255,255,255,0.14);
                          -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 20, 0.20, 0, 6);
                      """);

    Label title = new Label(notification.type());
    title.setStyle("""
                           -fx-text-fill: white;
                           -fx-font-size: 14px;
                           -fx-font-weight: bold;
                       """);

    Label message = new Label(notification.message());
    message.setWrapText(true);
    message.setStyle("""
                             -fx-text-fill: rgba(255,255,255,0.88);
                             -fx-font-size: 12px;
                         """);

    root.getChildren().addAll(title, message);
    popup.getContent().add(root);

    root.applyCss();
    root.layout();

    double popupWidth = root.prefWidth(-1);
    double popupHeight = root.prefHeight(-1);

    double marginRight = 10;
    double marginBottom = 10;

    double x = ownerStage.getX() + ownerStage.getWidth() - popupWidth - marginRight;
    double y = ownerStage.getY() + ownerStage.getHeight() - popupHeight - marginBottom;

    popup.show(ownerStage, x, y);

    PauseTransition delay = new PauseTransition(Duration.seconds(4));
    delay.setOnFinished(e -> popup.hide());
    delay.play();
  }

}