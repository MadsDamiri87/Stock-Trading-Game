package business.services.mapping;

import business.dto.TransactionDTO;
import entities.Transaction;

public class TransactionMapper
{
  public static TransactionDTO toTransactionDTO(Transaction transaction)
  {
    return new TransactionDTO(transaction.stockSymbol(), transaction.type(),
                              transaction.quantity(),
                              transaction.pricePerShare(),
                              transaction.totalAmount(), transaction.fee(),
                              transaction.timestamp());
  }
}
