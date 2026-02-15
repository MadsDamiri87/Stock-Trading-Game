package entities;

import java.math.BigDecimal;
import java.util.UUID;

public class Portfolio {

    private final UUID portfolioId;
    private BigDecimal currentBalance;

    //2 constructors i tilfælde af, at vi skal lade DBsen stå for id's

    public Portfolio(UUID portfolioId, BigDecimal currentBalance) {

       if (portfolioId == null) {
           this.portfolioId    = UUID.randomUUID();
       }else {this.portfolioId = portfolioId;}

        this.currentBalance = currentBalance;
    }

}
