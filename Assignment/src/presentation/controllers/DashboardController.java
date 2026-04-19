package presentation.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import presentation.viewmodels.DashboardViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable
{
  @FXML private BorderPane rootPane;
  @FXML private HBox titleBar;
  @FXML private StackPane sidebarContainer;
  @FXML private VBox sidebar;

  @FXML private Label sidebarTitleLabel;
  @FXML private Label sidebarSubtitleLabel;

  @FXML private Button dashboardButton;
  @FXML private Button portfolioButton;
  @FXML private Button buyButton;
  @FXML private Button sellButton;
  @FXML private Button marketButton;
  @FXML private Button settingsButton;
  @FXML private Button closeAppButton;

  private final DashboardViewModel viewModel;

  private double xOffset = 0;
  private double yOffset = 0;

  public DashboardController(DashboardViewModel viewModel)
  {
    this.viewModel = viewModel;
  }

  @Override public void initialize(URL location, ResourceBundle resources)
  {
    setupWindowDragging();
    setupBindings();

    viewModel.activeViewProperty().addListener((obs, oldValue, newValue) -> updateActiveMenu());
    viewModel.sidebarExpandedProperty()
             .addListener((obs, oldValue, newValue) -> updateSidebarPresentation());

    updateActiveMenu();
    updateSidebarPresentation();
  }

  private void setupWindowDragging()
  {
    if (titleBar != null && rootPane != null)
    {
      titleBar.setOnMousePressed(event -> {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
      });

      titleBar.setOnMouseDragged(event -> {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
      });
    }
  }

  private void setupBindings()
  {
    sidebarTitleLabel.textProperty().bind(viewModel.sidebarTitleProperty());
    sidebarSubtitleLabel.textProperty().bind(viewModel.sidebarSubtitleProperty());

    dashboardButton.textProperty().bind(viewModel.dashboardButtonTextProperty());
    portfolioButton.textProperty().bind(viewModel.portfolioButtonTextProperty());
    buyButton.textProperty().bind(viewModel.buyButtonTextProperty());
    sellButton.textProperty().bind(viewModel.sellButtonTextProperty());
    marketButton.textProperty().bind(viewModel.marketButtonTextProperty());
    settingsButton.textProperty().bind(viewModel.settingsButtonTextProperty());
    closeAppButton.textProperty().bind(viewModel.closeAppButtonTextProperty());
  }

  @FXML private void handleToggleSidebarAreaClick()
  {
    viewModel.toggleSidebar();

    double targetWidth = viewModel.getTargetSidebarWidth();

    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(220),
                                                  new KeyValue(sidebarContainer.prefWidthProperty(),
                                                               targetWidth),
                                                  new KeyValue(sidebarContainer.minWidthProperty(),
                                                               targetWidth),
                                                  new KeyValue(sidebarContainer.maxWidthProperty(),
                                                               targetWidth)));
    timeline.play();
  }

  private void updateSidebarPresentation()
  {
    if (viewModel.isSidebarExpanded())
    {
      sidebar.getStyleClass().remove("sidebar-collapsed");
    }
    else if (!sidebar.getStyleClass().contains("sidebar-collapsed"))
    {
      sidebar.getStyleClass().add("sidebar-collapsed");
    }
  }

  private void updateActiveMenu()
  {
    resetButtonStyle(dashboardButton);
    resetButtonStyle(portfolioButton);
    resetButtonStyle(buyButton);
    resetButtonStyle(sellButton);
    resetButtonStyle(marketButton);

    String activeView = viewModel.activeViewProperty().get();

    if ("dashboard".equals(activeView))
      setActive(dashboardButton);
    else if ("portfolio".equals(activeView))
      setActive(portfolioButton);
    else if ("buy".equals(activeView))
      setActive(buyButton);
    else if ("sell".equals(activeView))
      setActive(sellButton);
    else if ("market".equals(activeView))
      setActive(marketButton);
  }

  private void resetButtonStyle(Button button)
  {
    if (button == null)
      return;

    button.getStyleClass().remove("menu-button-primary");
    if (!button.getStyleClass().contains("menu-button-secondary"))
    {
      button.getStyleClass().add("menu-button-secondary");
    }
  }

  private void setActive(Button button)
  {
    if (button == null)
      return;

    button.getStyleClass().remove("menu-button-secondary");
    if (!button.getStyleClass().contains("menu-button-primary"))
    {
      button.getStyleClass().add("menu-button-primary");
    }
  }

  @FXML private void handleOpenDashboard()
  {
    viewModel.openDashboardHome();
  }

  @FXML private void handleOpenPortfolio()
  {
    viewModel.openPortfolio();
  }

  @FXML private void handleBuyStocks()
  {
    viewModel.buyStocks();
  }

  @FXML private void handleSellStocks()
  {
    viewModel.sellStocks();
  }

  @FXML private void handleViewMarket()
  {
    viewModel.openMarket();
  }

  @FXML private void startStopMarket()
  {
    viewModel.toggleMarketRunning();
  }

  @FXML private void handleMinimize()
  {
    ((Stage) rootPane.getScene().getWindow()).setIconified(true);
  }

  @FXML private void handleClose()
  {
    ((Stage) rootPane.getScene().getWindow()).close();
  }
}