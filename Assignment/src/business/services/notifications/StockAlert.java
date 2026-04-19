package business.services.notifications;

public record StockAlert(StockAlertType type, String stockSymbol, String message)
{
}
