package shared.logging;

public class Logger
{

  private static Logger instance;
  private LogOutput output;
  private final String className = getClass().getSimpleName();

  private Logger()
  {
    output = new ConsoleLogOutput();
  }

  public static synchronized Logger getInstance()
  {
    if (instance == null)
    {
      instance = new Logger();
    }
    return instance;
  }

  public synchronized void log(String level, String message)
  {
    String className = Thread.currentThread().getStackTrace()[3].getClassName();

    className = className.substring(className.lastIndexOf('.') + 1);

    output.log(className, level, message);
  }

  public void setOutput(LogOutput output)
  {
    this.output = output;
  }

}
