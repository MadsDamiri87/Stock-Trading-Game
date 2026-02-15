package entities;

import java.util.UUID;

public class OwnedStock {

    private final UUID ownedStockId;
    private UUID portfolioId;
    private String stockSymbolId;
    private int numberOfShares;

    public OwnedStock(UUID ownedStockId, UUID portfolioId,
                      String stockSymbolId, int numberOfShares) {

        this.ownedStockId   = ownedStockId;
        this.portfolioId    = portfolioId;
        this.stockSymbolId  = stockSymbolId;
        this.numberOfShares = numberOfShares;
    }
}
