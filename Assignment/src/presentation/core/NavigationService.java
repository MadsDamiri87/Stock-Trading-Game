package presentation.core;

public interface NavigationService
{

  //  TODO:
  //      Muligvis implementer interfacet i stedet for at bruge for mange af de samme
  //      metoder på tværs af klasser og lade viewmodels kende hinanden?..
  //      [undersøg hvad der giver mening]
  //      Svar: Adatper pattern.

  void openDashboardHome();
  void openPortfolio();
  void openBuyStocksView();
  void openSellStocksView();
  void openMarketView();
  void setTraderName(String traderName);
}