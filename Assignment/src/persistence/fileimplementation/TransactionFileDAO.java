package persistence.fileimplementation;

import entities.Transaction;
import persistence.interfaces.TransactionDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TransactionFileDAO implements TransactionDAO
{
  private final FileUnitOfWork uow;

  public TransactionFileDAO(FileUnitOfWork uow)
  {
    this.uow = uow;
  }

  @Override public Optional<Transaction> getById(UUID transactionId)
  {
    for (Transaction transaction : uow.getTransactions())
    {
      if (transaction.transactionId().equals(transactionId))
      {
        return Optional.of(transaction);
      }
    }
    return Optional.empty();
  }

  @Override public void create(Transaction transaction)
  {
    uow.getTransactions().add(transaction);
  }

  @Override public List<Transaction> getAll()
  {
    return uow.getTransactions();
  }

  @Override public List<Transaction> getByPortfolioId(UUID portfolioId, int offset, int limit)
  {
    return getAll().stream().filter((t -> t.portfolioId().equals(portfolioId))).skip(offset)
                   .limit(limit).toList();
  }

  @Override public List<Transaction> getByStockSymbol(String stockSymbol)
  {
    List<Transaction> result = new ArrayList<>();

    for (Transaction transaction : uow.getTransactions())
    {
      if (transaction.stockSymbol().equalsIgnoreCase(stockSymbol))
      {
        result.add(transaction);
      }
    }

    return result;
  }
}