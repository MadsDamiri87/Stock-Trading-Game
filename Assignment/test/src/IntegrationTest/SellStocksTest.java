package IntegrationTest;

import business.services.interfaces.PortfolioServiceInterface;
import business.services.interfaces.TradingServiceInterface;
import javafx.application.Platform;
import org.junit.jupiter.api.*;
import persistence.fileimplementation.FileUnitOfWork;
import persistence.interfaces.PortfolioDAO;
import persistence.interfaces.StockDAO;
import persistence.interfaces.TransactionDAO;
import presentation.state.UserSession;
import presentation.viewmodels.BuyStocksViewModel;

import java.util.UUID;

public class SellStocksTest
{
  String testDirPath;

  FileUnitOfWork unitOfWork;
  PortfolioDAO portfolioDAO;
  StockDAO stockDAO;
  TransactionDAO transactionDAO;

  PortfolioServiceInterface portfolioServiceInterface;
  TradingServiceInterface tradingServiceInterface;
  UserSession userSession;

  BuyStocksViewModel viewModel;

  @BeforeAll static void initToolKit()
  {
    Platform.startup(() -> {
    });
  }

  @BeforeEach void setup()
  {
    testDirPath = "test-" + UUID.randomUUID();
  }

  @AfterEach void cleanup()
  {
//    delete test folder
  }

  @Nested
  class GivenValidBuyOrder{
    @BeforeEach void act()
    {
//      Select stock
//      set shares
//      call vm.buy()
    }

    @Test
    void portfolioContainsBoughtStock() {}

    @Test
    void balanceIsReduced() {}

    @Test
    void transactionIsStored() {}
  }

  //  ny folder til path til hver test

}
