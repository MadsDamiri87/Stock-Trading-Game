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
    Path logPath = Path.of("logs/test-application.log");
    Files.deleteIfExists(logPath);

    Logger logger = Logger.getInstance();
    logger.setOutput(new FileLogOutputAdapter(logPath.toString(), "INFO"));

    logger.log("INFO", "Adapter test message");

    String content = Files.readString(logPath);

    assertTrue(content.contains("Adapter test message"));
    assertTrue(content.contains("INFO"));

    logger.setOutput(new ConsoleLogOutput());
    Files.deleteIfExists(logPath);
  }

  @Test
  void shouldRespectMinimumLogLevel() throws Exception
  {
    Path logPath = Path.of("logs/test-level.log");
    Files.deleteIfExists(logPath);

    Logger logger = Logger.getInstance();
    logger.setOutput(new FileLogOutputAdapter(logPath.toString(), "ERROR"));

    logger.log("INFO", "Should not appear");
    logger.log("ERROR", "Should appear");

    String content = Files.readString(logPath);

    assertFalse(content.contains("Should not appear"));
    assertTrue(content.contains("Should appear"));

    logger.setOutput(new ConsoleLogOutput());
    Files.deleteIfExists(logPath);
  }

  @Test
  void shouldWriteLogToFile() throws Exception
  {
    Path logPath = Path.of("logs/test-file.log");

    // cleanup før test
    Files.deleteIfExists(logPath);

    Logger logger = Logger.getInstance();
    logger.setOutput(new FileLogOutputAdapter(logPath.toString(), "INFO"));

    // Act
    logger.log("INFO", "FILE TEST");

    // Assert
    assertTrue(Files.exists(logPath), "Log file should be created");

    String content = Files.readString(logPath);
    assertTrue(content.contains("FILE TEST"), "File should contain log message");

    // cleanup efter test
    logger.setOutput(new ConsoleLogOutput());
    Files.deleteIfExists(logPath);
  }
}
