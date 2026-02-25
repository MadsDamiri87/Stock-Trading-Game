package entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transaction(

//  getters = transaction.type - Not transaction.getType

    UUID transactionId,
    UUID portfolioId,
    String stockSymbol,
    String type,
    int quantity,
    BigDecimal pricePerShare,
    BigDecimal totalAmount,
    BigDecimal fee,
    Instant timestamp

) {}
