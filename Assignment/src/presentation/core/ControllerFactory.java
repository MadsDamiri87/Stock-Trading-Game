package presentation.core;

import javafx.util.Callback;
import presentation.controllers.BuyStocksController;
import presentation.controllers.DashboardController;
import presentation.controllers.DashboardHomeController;
import presentation.controllers.PopUpWelcomeController;
import presentation.controllers.PortfolioController;
import presentation.controllers.SellStocksController;
import presentation.controllers.StockMarketController;
import shared.logging.Logger;

public class ControllerFactory implements Callback<Class<?>, Object>
{
  private final ApplicationContext context;
  private final Logger logger = Logger.getInstance();

  public ControllerFactory(ApplicationContext context)
  {
    this.context = context;
  }

  @Override public Object call(Class<?> controllerClass)
  {
    if (controllerClass == PopUpWelcomeController.class)
    {
      return new PopUpWelcomeController(context.getPopUpWelcomeViewModel());
    }
    else if (controllerClass == DashboardController.class)
    {
      return new DashboardController(context.getDashboardViewModel());
    }
    else if (controllerClass == DashboardHomeController.class)
    {
      return new DashboardHomeController(context.getDashboardViewModel());
    }
    else if (controllerClass == PortfolioController.class)
    {
      return new PortfolioController(context.getPortfolioViewModel());
    }
    else if (controllerClass == StockMarketController.class)
    {
      return new StockMarketController(context.getStockMarketViewModel());
    }
    else if (controllerClass == BuyStocksController.class)
    {
      return new BuyStocksController(context.getBuyStocksViewModel());
    }
    else if (controllerClass == SellStocksController.class)
    {
      return new SellStocksController(context.getSellStocksViewModel());
    }

    logger.log("ERROR", "Couldn't create controller: " + controllerClass.getName());
    throw new IllegalArgumentException("Unknown controller class: " + controllerClass.getName());
  }
}