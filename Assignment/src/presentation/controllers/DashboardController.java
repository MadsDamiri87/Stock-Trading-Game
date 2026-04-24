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
import presentation.core.NavigationService;
import presentation.viewmodels.DashboardViewModel;

import java.net.URL;
import java.util.Map;
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

  private Map<String, Button> navButtons;

  private final DashboardViewModel viewModel;
  private final NavigationService navigationService;

  private double xOffset = 0;
  private double yOffset = 0;

  public DashboardController(DashboardViewModel viewModel, NavigationService navigationService)
  {
    this.viewModel = viewModel;
    this.navigationService = navigationService;
  }

  @Override public void initialize(URL location, ResourceBundle resources)
  {
    setupWindowDragging();
    initializeNavigationButtons();
    setupBindings();
    setupListeners();

    updateActiveMenu(viewModel.activeViewProperty().get());
    updateSidebarPresentation();
  }

  private void setupListeners()
  {
    viewModel.activeViewProperty().addListener(
        (obs, oldValue, newValue) -> updateActiveMenu(newValue)
    );

    viewModel.sidebarExpandedProperty().addListener(
        (obs, oldValue, newValue) -> updateSidebarPresentation()
    );
  }

  private void initializeNavigationButtons()
  {
    navButtons = Map.of(
        "dashboard", dashboardButton,
        "portfolio", portfolioButton,
        "buy", buyButton,
        "sell", sellButton,
        "market", marketButton
    );
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

  private void updateActiveMenu(String activeView)
  {
    resetAllNavigationButtons();

    Button activeButton = navButtons.get(activeView);

    if (activeButton != null)
    {
      setActive(activeButton);
    }
  }

  private void resetAllNavigationButtons()
  {
    for (Button button : navButtons.values())
    {
      resetButtonStyle(button);
    }
  }

  private void resetButtonStyle(Button button)
  {
    if (button == null)
    {
      return;
    }

    button.getStyleClass().remove("menu-button-primary");

    if (!button.getStyleClass().contains("menu-button-secondary"))
    {
      button.getStyleClass().add("menu-button-secondary");
    }
  }

  private void setActive(Button button)
  {
    if (button == null)
    {
      return;
    }

    button.getStyleClass().remove("menu-button-secondary");

    if (!button.getStyleClass().contains("menu-button-primary"))
    {
      button.getStyleClass().add("menu-button-primary");
    }
  }

  @FXML private void handleToggleSidebarAreaClick()
  {
    viewModel.toggleSidebar();

    double targetWidth = viewModel.getTargetSidebarWidth();

    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(220),
                                                  new KeyValue(sidebarContainer.prefWidthProperty(), targetWidth),
                                                  new KeyValue(sidebarContainer.minWidthProperty(), targetWidth),
                                                  new KeyValue(sidebarContainer.maxWidthProperty(), targetWidth)));

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

  @FXML private void handleOpenDashboard()
  {
    navigationService.openDashboardHome();
  }

  @FXML private void handleOpenPortfolio()
  {
    navigationService.openPortfolio();
  }

  @FXML private void handleBuyStocks()
  {
    navigationService.openBuyStocksView();
  }

  @FXML private void handleSellStocks()
  {
    navigationService.openSellStocksView();
  }

  @FXML private void handleViewMarket()
  {
    navigationService.openMarketView();
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