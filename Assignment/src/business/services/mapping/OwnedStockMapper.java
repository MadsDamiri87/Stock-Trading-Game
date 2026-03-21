package business.services.mapping;

import business.dto.OwnedStockDTO;
import entities.OwnedStock;

public class OwnedStockMapper
{
  public static OwnedStockDTO toOwnedStockDTO(OwnedStock ownedStock)
  {
    return new OwnedStockDTO(ownedStock.getStockSymbol(),
                             ownedStock.getNumberOfShares());
  }
}
