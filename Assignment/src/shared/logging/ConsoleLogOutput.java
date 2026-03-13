package shared.logging;

public class ConsoleLogOutput implements LogOutput
{


  @Override public synchronized void log(String className, String level, String message)
  {
    System.out.println("[" + className + "] | " + level + " | " + message);
  }
}
