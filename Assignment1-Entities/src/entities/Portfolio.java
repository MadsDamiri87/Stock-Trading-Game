package entities;

import java.math.BigDecimal;
import java.util.UUID;

public class Portfolio {

    private final UUID id;
    private BigDecimal currentBalance;

    public Portfolio(UUID id, BigDecimal currentBalance) {

       if (id == null) {
           this.id    = UUID.randomUUID();
       }else {this.id = id;}

        this.currentBalance = currentBalance;
    }

}
