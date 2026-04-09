package presentation;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import presentation.core.ApplicationContext;
import presentation.core.ViewManager;

public class MainApp extends Application
{
  @Override
  public void start(Stage stage) throws Exception
  {
    stage.initStyle(StageStyle.TRANSPARENT);

    ApplicationContext context = new ApplicationContext();
    ViewManager.init(stage, "PopUpWelcomeView", context);
  }

  public static void main(String[] args)
  {
    launch();
  }
}