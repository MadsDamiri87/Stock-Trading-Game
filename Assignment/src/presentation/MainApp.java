package presentation;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import presentation.core.ApplicationContext;
import presentation.core.ViewManager;
import presentation.notifications.NotificationPopupManager;

public class MainApp extends Application
{
  @Override public void start(Stage stage) throws Exception
  {
    stage.initStyle(StageStyle.TRANSPARENT);

    ApplicationContext context = new ApplicationContext();
    ViewManager.init(stage, "PopUpWelcomeView", context);

    NotificationPopupManager popupManager = new NotificationPopupManager(
        context.getNotificationService(), stage);
    popupManager.start();

    context.getNotificationService().notify("TEST", "Popup system is active");
  }

  public static void main(String[] args)
  {
    launch();
  }
}