package persistence.mocks;

import entities.Transaction;
import persistence.interfaces.TransactionDAO;

import java.util.*;

public class MockTransactionDAO implements TransactionDAO
{
  private Map<UUID, Transaction> data = new HashMap<>();

  @Override public Optional<Transaction> getById(UUID transactionId)
  {
    return Optional.ofNullable(data.get(transactionId));
  }

  @Override public void create(Transaction transaction)
  {
    data.put(transaction.transactionId(), transaction);
  }

  @Override public List<Transaction> getAll()
  {
    return new ArrayList<>(data.values());
  }

  @Override public List<Transaction> getByPortfolioId(UUID portfolioId, int page, int size)
  {
    int offset = page * size;

    return data.values().stream()
        .filter(s->s.portfolioId().equals(portfolioId))
        .skip(offset)
        .limit(size)
        .toList();
  }

  @Override public List<Transaction> getByStockSymbol(String stockSymbol)
  {
    return data.values().stream()
        .filter(s->s.stockSymbol().equals(stockSymbol))
        .toList();
  }
}
