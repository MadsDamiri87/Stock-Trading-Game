package shared.logging;

public class RunAppTEST
{
  public static void main(String[] args)
  {
    Logger logger = Logger.getInstance();

    // Linje 13+14 er ikke nødvendige da jeg allerede har
    // output sat til newConsoleLogOutput inde i Logger.
    // De er kun med for at demonstrere at setOutput virker.
    LogOutput consoleOutPut = new ConsoleLogOutput();
    logger.setOutput(consoleOutPut);

    logger.log("INFO", "Application started");
    logger.log("WARNING", "Stock not found in database");

    try
    {
      throw new RuntimeException("Database connection failed");
    }
    catch (Exception e)
    {
      logger.log("ERROR", "Failed to save data: " + e.getMessage());
    }

  }
}
