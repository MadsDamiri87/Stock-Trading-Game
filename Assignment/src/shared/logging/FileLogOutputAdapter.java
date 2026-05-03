package shared.logging;

import provided.FileLogOutputter;

public class FileLogOutputAdapter implements LogOutput
{
  private final FileLogOutputter fileLogOutputter;

  public FileLogOutputAdapter(String path, String level)
  {
    this.fileLogOutputter = new FileLogOutputter(path, level);
  }

  @Override public void log(String className, String level, String message)
  {
    String formattedMessage = "[" + className + "] " + message;

    switch (level.toUpperCase())
    {
      case "INFO" -> fileLogOutputter.logInfo(formattedMessage);
      case "WARNING" -> fileLogOutputter.logWarning(formattedMessage);
      case "ERROR" -> fileLogOutputter.logError(formattedMessage);
      default -> fileLogOutputter.logInfo(formattedMessage);
    }
  }
}
