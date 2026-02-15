package entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Transaction {

    private final UUID transactionId;
    private UUID portfolioId;
    private String stockSymbol;
    private String type;
    private int quantity;
    private BigDecimal pricePerShare;
    private BigDecimal totalAmount;
    private BigDecimal fee;
    private Instant timestamp;

    public Transaction(UUID transactionId, UUID portfolioId, String stockSymbol, String type,
                       int quantity, BigDecimal pricePerShare, BigDecimal totalAmount,
                       BigDecimal fee, Instant timestamp) {

        this.transactionId = transactionId;
        this.portfolioId   = portfolioId;
        this.stockSymbol   = stockSymbol;
        this.type          = type;
        this.quantity      = quantity;
        this.pricePerShare = pricePerShare;
        this.totalAmount   = totalAmount;
        this.fee           = fee;
        this.timestamp     = timestamp;
    }
}
