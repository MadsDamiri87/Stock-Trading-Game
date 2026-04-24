package IntegrationTest;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;

public class BuyStocksTest
{
  @BeforeAll
  static void initToolKit()
  {
    Platform.startup(()-> {});
  }




}
