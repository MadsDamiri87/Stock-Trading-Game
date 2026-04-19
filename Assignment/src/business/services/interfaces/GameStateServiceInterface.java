package business.services.interfaces;

public interface GameStateServiceInterface
{
    void startGame();
    void stopGame();
    void resetGame();
    void updateMarket();
    boolean isGameRunning();
    boolean isMarketInitialized();
}
