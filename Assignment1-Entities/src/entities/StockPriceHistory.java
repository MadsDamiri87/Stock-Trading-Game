package entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class StockPriceHistory {
    private final UUID stockPriceHisId;
    private String symbolId;
    private final BigDecimal price;
    private final Instant timestamp;

//lad databasen stå for UUID, derfor ikke hav den med i konstructoren. overload constructor.


    public StockPriceHistory(UUID stockPriceHisId, BigDecimal price,
                             Instant timestamp, String symbolId) {

        this.stockPriceHisId = stockPriceHisId;
        this.price           = price;
        this.timestamp       = timestamp;
        this.symbolId        = symbolId;
    }
}
