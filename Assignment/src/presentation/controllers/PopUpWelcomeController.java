package presentation.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import presentation.viewmodels.PopUpWelcomeViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public class PopUpWelcomeController implements Initializable
{
  @FXML private TextField nameField;

  private final PopUpWelcomeViewModel viewModel;

  public PopUpWelcomeController(PopUpWelcomeViewModel viewModel)
  {
    this.viewModel = viewModel;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    nameField.textProperty().bindBidirectional(viewModel.traderNameProperty());
  }

  @FXML
  private void handleContinue()
  {
    viewModel.continueToDashboard();
  }
}