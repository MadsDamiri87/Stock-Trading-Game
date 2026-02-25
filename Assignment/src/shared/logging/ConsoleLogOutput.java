package shared.logging;

public class ConsoleLogOutput implements LogOutput
{

  @Override public synchronized void log(String level, String message)
  {
    System.out.println("[" + level + "] " + message);
  }
}
