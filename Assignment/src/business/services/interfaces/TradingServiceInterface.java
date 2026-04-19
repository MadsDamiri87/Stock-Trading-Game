package business.services.interfaces;

import business.dto.TradeRequestDTO;

public interface TradingServiceInterface
{
    void buyStock(TradeRequestDTO request);
    void sellStock(TradeRequestDTO request);
}
