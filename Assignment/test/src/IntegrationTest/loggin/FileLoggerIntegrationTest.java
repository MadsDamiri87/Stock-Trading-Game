package IntegrationTest.loggin;

import org.junit.jupiter.api.Test;
import shared.logging.ConsoleLogOutput;
import shared.logging.FileLogOutputAdapter;
import shared.logging.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileLoggerIntegrationTest
{
  @Test void shouldWriteLogMessageToFile() throws Exception
  {
//    arrange
    Path logPath = Path.of("logs/test-application.log");
    Files.deleteIfExists(logPath);

    Logger logger = Logger.getInstance();

   try
   {
//    act
    logger.setOutput(new FileLogOutputAdapter(logPath.toString(), "INFO"));

    logger.log("INFO", "Adapter test message");

//    assert
    String content = Files.readString(logPath);

    assertTrue(content.contains("Adapter test message"));
    assertTrue(content.contains("INFO"));
   }
   finally
   {
     logger.setOutput(new ConsoleLogOutput());
     Files.deleteIfExists(logPath);
   }
  }

  @Test
  void shouldRespectMinimumLogLevel() throws Exception
  {
    // arrange + cleanup før test
    Path logPath = Path.of("logs/test-level.log");
    Files.deleteIfExists(logPath);


    Logger logger = Logger.getInstance();

    /* try-block i tilfælde af testen fejler før den bliver nulstillet igen. (så bliver
    loggeren stående med FileLogOutputAdapter) */
    try
    {
      // act
      logger.setOutput(new FileLogOutputAdapter(logPath.toString(), "ERROR"));


      logger.log("INFO", "Should not appear");
      logger.log("ERROR", "Should appear");

      // assert
      String content = Files.readString(logPath);

      assertFalse(content.contains("Should not appear"));
      assertTrue(content.contains("Should appear"));
    }
    finally
    {
      logger.setOutput(new ConsoleLogOutput());
      Files.deleteIfExists(logPath);
    }
  }

  @Test
  void shouldWriteLogToFile() throws Exception
  {
    // arrange + cleanup før test
    Path logPath = Path.of("logs/test-file.log");
    Files.deleteIfExists(logPath);

    Logger logger = Logger.getInstance();

    try
    {
      logger.setOutput(new FileLogOutputAdapter(logPath.toString(), "INFO"));

      // Act
      logger.log("INFO", "FILE TEST");

      // Assert
      assertTrue(Files.exists(logPath), "Log file should be created");

      String content = Files.readString(logPath);
      assertTrue(content.contains("FILE TEST"), "File should contain log message");

    }
    finally
    {
      // cleanup efter test
      logger.setOutput(new ConsoleLogOutput());
      Files.deleteIfExists(logPath);
    }

  }
}
