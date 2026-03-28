package persistence.interfaces;

import entities.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionDAO
{
  Optional<Transaction> getById(UUID transactionId);

  void create(Transaction transaction);

  List<Transaction> getAll();

  List<Transaction> getByPortfolioId(UUID portfolioId, int page, int size);

  List<Transaction> getByStockSymbol(String stockSymbol);
}