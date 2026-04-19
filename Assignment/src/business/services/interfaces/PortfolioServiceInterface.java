package business.services.interfaces;

import business.dto.PortfolioDTO;
import business.dto.StockDTO;
import business.dto.TransactionDTO;

import java.util.List;
import java.util.UUID;

public interface PortfolioServiceInterface
{
    PortfolioDTO getPortfolio(UUID portfolioId);
    List<TransactionDTO> getTransactionHistory(UUID portfolioId, int page, int size);
    List<StockDTO> getAvailableStocks();

}
