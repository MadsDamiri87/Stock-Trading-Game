package presentation.core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import shared.logging.Logger;

import java.io.IOException;

public class ViewManager
{
  private static final String FXML_DIRECTORY_PATH = "/fxml/";
  private static final Logger logger = Logger.getInstance();

  private static Stage primaryStage;
  private static BorderPane mainLayout;

  private static ApplicationContext context;
  private static ControllerFactory factory;

  public static void init(Stage stage, String initialView, ApplicationContext context) throws IOException
  {
    primaryStage = stage;
    ViewManager.context = context;
    factory = new ControllerFactory(context);

    Parent root = loadFXML(initialView);

    Scene scene = new Scene(root, 1050, 740);
    scene.setFill(Color.TRANSPARENT);
    primaryStage.setScene(scene);
    primaryStage.setTitle("StockTrading - A Trading System");
    primaryStage.show();
  }

  public static void openMainApplication(String shellView)
  {
    try
    {
      Parent root = loadFXML(shellView);

      if (!(root instanceof BorderPane))
      {
        throw new IllegalStateException(shellView + " must have BorderPane as root.");
      }

      mainLayout = (BorderPane) root;
      primaryStage.getScene().setRoot(mainLayout);
    }
    catch (IOException e)
    {
      logger.log("ERROR", "Cannot open main application shell: " + shellView);
      e.printStackTrace();
      Alert error = new Alert(Alert.AlertType.ERROR,
                              "Cannot open main application shell: " + shellView);
      error.show();
    }
  }

  public static void showView(String viewName)
  {
    if (mainLayout == null)
    {
      logger.log("ERROR", "Main layout is not initialized.");
      Alert error = new Alert(Alert.AlertType.ERROR,
                              "Main layout is not initialized.");
      error.show();
      return;
    }

    try
    {
      Parent root = loadFXML(viewName);
      mainLayout.setCenter(root);
    }
    catch (IOException e)
    {
      logger.log("ERROR", "Cannot load view: " + viewName);
      e.printStackTrace();
      Alert error = new Alert(Alert.AlertType.ERROR, "Cannot load view: " + viewName);
      error.show();
    }
  }

  private static Parent loadFXML(String viewName) throws IOException
  {
    FXMLLoader loader =
        new FXMLLoader(ViewManager.class.getResource(FXML_DIRECTORY_PATH + viewName + ".fxml"));
    loader.setControllerFactory(factory);
    return loader.load();
  }
}