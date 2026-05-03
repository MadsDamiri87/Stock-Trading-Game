package provided;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CustomAlertBox
{
  public enum AlertType {
    INFO,
    WARNING,
    ERROR
  }

  /**
   * Shows a custom alert dialog with the specified message, title, and type.
   * Opens a new Stage with custom styling based on the alert type.
   * Does not block the application - allows background updates to continue.
   *
   * @param message The message to display
   * @param title The title of the alert window
   * @param type The type of alert (INFO, WARNING, ERROR)
   */
  public void showAlert(String message, String title, AlertType type) {
    // Ensure we're on the JavaFX Application Thread
    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(() -> showAlert(message, title, type));
      return;
    }

    // Create a new Stage for the alert
    Stage alertStage = new Stage();
    alertStage.initModality(Modality.NONE); // Non-modal: allows background updates to continue
    alertStage.initStyle(StageStyle.UTILITY);
    alertStage.setTitle(title);
    alertStage.setResizable(false);
    alertStage.setAlwaysOnTop(true); // Keep alert visible on top

    // Create root pane
    VBox root = new VBox(15);
    root.setPadding(new Insets(20));
    root.setAlignment(Pos.CENTER);
    root.setMinWidth(400);
    root.setMinHeight(150);

    // Determine colors and icon based on type
    Color borderColor;
    Color backgroundColor;
    String typeLabel;

    switch (type) {
      case ERROR:
        borderColor = Color.web("#e74c3c");
        backgroundColor = Color.web("#ffebee");
        typeLabel = "ERROR";
        break;
      case WARNING:
        borderColor = Color.web("#f39c12");
        backgroundColor = Color.web("#fff3cd");
        typeLabel = "WARNING";
        break;
      case INFO:
      default:
        borderColor = Color.web("#3498db");
        backgroundColor = Color.web("#e3f2fd");
        typeLabel = "INFO";
        break;
    }

    // Style the root pane
    root.setStyle(String.format(
        "-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 3px;",
        toHex(backgroundColor), toHex(borderColor)
    ));

    // Type label
    Label typeLabelControl = new Label(typeLabel);
    typeLabelControl.setStyle(String.format(
        "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: %s;",
        toHex(borderColor)
    ));

    // Message label
    Label messageLabel = new Label(message);
    messageLabel.setWrapText(true);
    messageLabel.setStyle("-fx-font-size: 14px;");
    messageLabel.setMaxWidth(360);

    // OK button
    Button okButton = new Button("OK");
    okButton.setStyle(String.format(
        "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20;",
        toHex(borderColor)
    ));
    okButton.setOnAction(e -> alertStage.close());

    // Button container
    HBox buttonBox = new HBox();
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.getChildren().add(okButton);

    // Add all components to root
    root.getChildren().addAll(typeLabelControl, messageLabel, buttonBox);

    // Create scene and show
    Scene scene = new Scene(root);
    alertStage.setScene(scene);
    alertStage.centerOnScreen();
    alertStage.show();
  }

  /**
   * Convert JavaFX Color to hex string for CSS.
   */
  private String toHex(Color color) {
    return String.format("#%02X%02X%02X",
                         (int)(color.getRed() * 255),
                         (int)(color.getGreen() * 255),
                         (int)(color.getBlue() * 255)
    );
  }

}
